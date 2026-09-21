package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.cartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import RetrofitClient
import com.example.prog7314p2.Models.OrderHistoryModel
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingCart : Fragment() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartItemsList = mutableListOf<cartItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCartItems = view.findViewById(R.id.rvCartItems)
        val btnCheckout = view.findViewById<Button>(R.id.checkoutButton)

        // Setup the adapter with a listener to remove items
        cartAdapter = CartAdapter(cartItemsList) { itemToRemove ->
            removeFromCart(itemToRemove.productId)
        }
        rvCartItems.adapter = cartAdapter

        // Fetch items from Firebase
        listenToCart()

        btnCheckout.setOnClickListener {
            if (cartItemsList.isEmpty()) {
                Toast.makeText(context, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            } else {
                processCheckout()
            }
        }
    }

    private var cartListener: ListenerRegistration? = null

    private fun listenToCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Remove old listener if one exists
        cartListener?.remove()

        // This listens to Firebase in REAL-TIME! (Requirement 3.1)
        cartListener = FirebaseFirestore.getInstance()
            .collection("Cart").document(userId)
            .collection("Items")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                // Safety check: Don't launch coroutine or update UI if fragment is not attached!
                if (!isAdded || view == null) return@addSnapshotListener

                // Use coroutines to fetch product details from Spring Boot API for each cart item
                lifecycleScope.launch {
                    val newItems = mutableListOf<cartItem>()
                    var totalPrice = 0.0

                    for (document in snapshot.documents) {
                        val productId = document.getLong("productId")?.toInt() ?: continue
                        val quantity = document.getLong("quantity")?.toInt() ?: 1

                        try {
                            // Fetch product info from Spring Boot PostgreSQL API
                            val product = RetrofitClient.instance.getProductDetails(productId)
                            val item = cartItem(
                                productId = product.id,
                                name = product.name,
                                price = product.price,
                                quantity = quantity,
                                imageUrl = product.imageUrl
                            )
                            newItems.add(item)
                            totalPrice += (product.price * quantity)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                    if (isAdded && view != null) {
                        cartItemsList.clear()
                        cartItemsList.addAll(newItems)
                        cartAdapter.notifyDataSetChanged()

                        // Update the UI with the new total
                        val tvTotal = view?.findViewById<TextView>(R.id.tvCartTotal)
                        tvTotal?.text = String.format(Locale.getDefault(), "Total: R %.2f", totalPrice)
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Crucial: Detach the Firebase listener when the view is destroyed to prevent crashes!
        cartListener?.remove()
    }

    private fun removeFromCart(productId: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Cart").document(userId)
            .collection("Items").document(productId.toString())
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun processCheckout() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        var totalCost = 0.0
        var totalItems = 0
        for (item in cartItemsList) {
            totalCost += (item.price * item.quantity)
            totalItems += item.quantity
        }

        val orderId = System.currentTimeMillis().toString().takeLast(6)
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val orderData = OrderHistoryModel(
            orderId = orderId,
            datePlaced = currentDate,
            totalCost = totalCost,
            eta = "3-5 Business Days",
            itemCount = totalItems,
            status = "Processing"
        )

        val db = FirebaseFirestore.getInstance()
        
        // 1. Save order to Firestore Users -> {userId} -> Orders -> {orderId}
        db.collection("Users").document(userId)
            .collection("Orders").document(orderId)
            .set(orderData)
            .addOnSuccessListener {
                // 2. Clear the cart from Firestore
                db.collection("Cart").document(userId)
                    .collection("Items")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                        
                        // 3. Navigate to OrderSucessFull fragment with bundle!
                        if (isAdded) {
                            val successFragment = OrderSucessFull()
                            val bundle = Bundle().apply {
                                putString("ORDER_ID", orderId)
                                putString("DATE_PLACED", currentDate)
                                putDouble("TOTAL_COST", totalCost)
                                putString("ETA", "3-5 Business Days")
                            }
                            successFragment.arguments = bundle

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, successFragment)
                                .commit()
                        }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Checkout failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}