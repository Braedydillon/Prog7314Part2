package com.example.prog7314p2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Firestore.FirebaseHelper
import com.example.prog7314p2.Models.cartItem
import kotlinx.coroutines.launch
import java.util.Locale
import RetrofitClient

class ShoppingCart : Fragment() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter

    private val cartItemsList = mutableListOf<cartItem>()

    private val firebaseHelper = FirebaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.fragment_shopping_cart,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        rvCartItems = view.findViewById(R.id.rvCartItems)

        val btnCheckout = view.findViewById<Button>(R.id.checkoutButton)

        // Setup the adapter
        cartAdapter = CartAdapter(cartItemsList) { itemToRemove ->

            removeFromCart(itemToRemove.productId)
        }

        rvCartItems.adapter = cartAdapter

        // Load cart from Firebase
        loadCart()

        btnCheckout.setOnClickListener {
            // TODO: Trigger Zach's checkout API later
            Toast.makeText(context,"Proceeding to checkout...",Toast.LENGTH_SHORT).show()
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
}