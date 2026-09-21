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
import com.google.firebase.firestore.ListenerRegistration
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
            // TODO: Trigger Zach's API here for Checkout!
            Toast.makeText(context, "Proceeding to checkout...", Toast.LENGTH_SHORT).show()
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
}