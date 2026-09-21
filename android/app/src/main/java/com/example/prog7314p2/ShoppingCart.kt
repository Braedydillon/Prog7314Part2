package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Firestore.FirebaseHelper
import com.example.prog7314p2.Models.cartItem
import kotlinx.coroutines.launch
import java.util.Locale
import RetrofitClient
import com.example.prog7314p2.Models.OrderHistoryModel
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date


class ShoppingCart : Fragment() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartItemsList = mutableListOf<cartItem>()

    private val firebaseHelper = FirebaseHelper()

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

        // Load cart from Firebase
        loadCart()

        btnCheckout.setOnClickListener {
            if (cartItemsList.isEmpty()) {
                Toast.makeText(context, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            } else {
                processCheckout()
            }
        }
    }

    private fun loadCart() {

        firebaseHelper.getCartItems(

            onSuccess = { cartItems ->

                // Make sure the fragment is still attached
                if (!isAdded || view == null) {
                    return@getCartItems
                }

                lifecycleScope.launch {

                    val newItems = mutableListOf<cartItem>()

                    var totalPrice = 0.0

                    // Get product details from Spring Boot API
                    for (cartItem in cartItems) {

                        val productId = cartItem.productId
                        val quantity = cartItem.quantity

                        try {

                            val product = RetrofitClient.instance.getProductDetails(productId)

                            val item = cartItem(
                                productId = product.id,
                                name = product.name,
                                price = product.price,
                                quantity = quantity,
                                imageUrl = product.imageUrl
                            )

                            newItems.add(item)

                            totalPrice += product.price * quantity

                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                    if (!isAdded || view == null) {
                        return@launch
                    }

                    // Update cart list
                    cartItemsList.clear()

                    cartItemsList.addAll(newItems)

                    cartAdapter.notifyDataSetChanged()

                    // Update total
                    val tvTotal =
                        view?.findViewById<TextView>(
                            R.id.tvCartTotal
                        )

                    tvTotal?.text = String.format(Locale.getDefault(), "Total: R %.2f",totalPrice)
                }
            },

            onFailure = { exception ->

                if (!isAdded) {
                    return@getCartItems
                }

                Toast.makeText(context,exception.message?: "Failed to load cart",Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun removeFromCart(productId: Int) {

        firebaseHelper.removeFromCart(

            productId = productId,

            onSuccess = {

                if (!isAdded) {
                    return@removeFromCart
                }

                Toast.makeText( context,"Removed from cart",Toast.LENGTH_SHORT).show()

                // Reload cart after removing item
                loadCart()
            },

            onFailure = { exception ->

                if (!isAdded) {
                    return@removeFromCart
                }

                Toast.makeText(context,exception.message?: "Failed to remove item",Toast.LENGTH_SHORT).show()
            }
        )
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