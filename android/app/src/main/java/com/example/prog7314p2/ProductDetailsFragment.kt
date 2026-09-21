package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.prog7314p2.Models.Product
import RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

class ProductDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Unpack the product ID sent from HomeFragment
        val product = arguments?.getSerializable("PRODUCT_DATA") as? Product
        val productId = product?.id
        
        if (productId != null) {
            fetchProductDetails(productId, view)
        }
    }

    private fun fetchProductDetails(productId: Int, view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch the full product details from the API
                val fetchedProduct = RetrofitClient.instance.getProductDetails(productId)
                
                // Find Views
                val ivImage = view.findViewById<ImageView>(R.id.ivDetailImage)
                val tvName = view.findViewById<TextView>(R.id.tvDetailName)
                val tvPrice = view.findViewById<TextView>(R.id.tvDetailPrice)
                val tvDesc = view.findViewById<TextView>(R.id.tvDetailDescription)
                val btnAddCart = view.findViewById<Button>(R.id.btnAddCart)

                // Set the Data
                tvName.text = fetchedProduct.name
                tvPrice.text = String.format(Locale.getDefault(), "R %.2f", fetchedProduct.price)
                tvDesc.text = fetchedProduct.description ?: "No description available."

                // Load Image
                val imageUrl = fetchedProduct.imageUrl
                if (!imageUrl.isNullOrEmpty() && imageUrl != "no-image-yet.com") {
                    ivImage.load(imageUrl) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                // Click listener for the Cart button
                btnAddCart.setOnClickListener {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val cartDocRef = FirebaseFirestore.getInstance()
                            .collection("Cart").document(userId)
                            .collection("Items").document(fetchedProduct.id.toString())

                        // Check if the item is already in the cart to increment quantity
                        cartDocRef.get().addOnSuccessListener { document ->
                            val currentQty = if (document.exists()) {
                                (document.getLong("quantity") ?: 1L).toInt()
                            } else {
                                0
                            }
                            val newQty = currentQty + 1

                            // Save ONLY productId and quantity in Firestore (matching your PDF Schema)
                            val cartItem = hashMapOf(
                                "productId" to fetchedProduct.id,
                                "quantity" to newQty
                            )

                            cartDocRef.set(cartItem)
                                .addOnSuccessListener {
                                    // Use context/view safely
                                    val currentContext = context
                                    val currentView = view
                                    if (currentContext != null && currentView != null && isAdded) {
                                        try {
                                            Snackbar.make(
                                                currentView,
                                                "${fetchedProduct.name} added to cart! (Qty: $newQty)",
                                                Snackbar.LENGTH_LONG
                                            ).setAction("GO TO CART") {
                                                parentFragmentManager.beginTransaction()
                                                    .replace(R.id.fragmentContainer, ShoppingCart())
                                                    .addToBackStack(null)
                                                    .commit()
                                            }.show()
                                        } catch (ex: Exception) {
                                            Toast.makeText(currentContext, "${fetchedProduct.name} added to cart!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to add to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(context, "Please log in to add to cart.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}