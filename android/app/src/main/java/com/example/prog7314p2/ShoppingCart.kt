package com.example.prog7314p2

import RetrofitClient
import android.os.Bundle
import android.util.Log
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
import com.example.prog7314p2.Models.CreateAddressRequest
import com.example.prog7314p2.Models.OrderItemRequest
import com.example.prog7314p2.Models.OrderRequest
import com.example.prog7314p2.Models.StreakStatus
import com.example.prog7314p2.Models.cartItem
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.util.Locale


class ShoppingCart : Fragment() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartItemsList = mutableListOf<cartItem>()
    private var cartTotal = 0.0
    private var streakStatus: StreakStatus? = null

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
        loadPoints()
        view.findViewById<MaterialSwitch>(R.id.switchUsePoints)
            .setOnCheckedChangeListener { _, _ -> updateTotal() }

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
                    cartTotal = totalPrice
                    updateTotal()
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

        lifecycleScope.launch {
            try{

                val addressId = RetrofitClient.instance.getAddresses().firstOrNull()?.id
                    ?: RetrofitClient.instance.createAddress(
                        CreateAddressRequest(
                            addressLine = "123 Street Street",
                            city = "City",
                            province = "Province",
                            postalCode = "1234"
                        )
                    ).id

                val orderItems = cartItemsList.map { OrderItemRequest(
                    productId = it.productId,
                    quantity = it.quantity) }
                val usePoints = view?.findViewById<MaterialSwitch>(R.id.switchUsePoints)?.isChecked == true
                val orderRequest = OrderRequest(
                    items = orderItems,
                    addressId = addressId,
                    pointsToSpend = if (usePoints) streakStatus?.pointsBalance ?: 0 else 0
                )
                val order = RetrofitClient.instance.placeOrder(orderRequest)

                firebaseHelper.clearCart(
                    onSuccess = { },
                    onFailure = { ex -> ex.printStackTrace() }
                )

                if (isAdded){
                    val successFragment = OrderSucessFull()
                    val bundle = Bundle().apply {
                        putString("ORDER_ID", order.id.toString())
                        putString("DATE_PLACED", order.createdAt)
                        putDouble("TOTAL_COST", order.total)
                        putString("ETA", order.estimatedDelivery ?: "3-5 Business Days")
                    }
                    successFragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, successFragment)
                        .commit()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = e.serverMessage() ?: e.message
                Toast.makeText(context, getString(R.string.checkout_failed, message), Toast.LENGTH_LONG).show()
                if ((e as? HttpException)?.code() == 409) loadPoints()
            }
        }
    }

    private fun loadPoints() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val status = RetrofitClient.instance.getStreakStatus()
                streakStatus = status

                val switch = view?.findViewById<MaterialSwitch>(R.id.switchUsePoints) ?: return@launch
                if (status.pointsBalance > 0) {
                    switch.text = getString(R.string.cart_use_points, status.pointsBalance)
                    switch.visibility = View.VISIBLE
                } else {
                    switch.isChecked = false
                    switch.visibility = View.GONE
                }
                updateTotal()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Streak", "Failed to load points", e)
            }
        }
    }

    private fun updateTotal() {
        val tvTotal = view?.findViewById<TextView>(R.id.tvCartTotal) ?: return
        val usePoints = view?.findViewById<MaterialSwitch>(R.id.switchUsePoints)?.isChecked == true
        val discount = if (usePoints) minOf(streakStatus?.balanceInRand ?: 0.0, cartTotal) else 0.0

        tvTotal.text = if (discount > 0) {
            getString(R.string.cart_total_with_points, cartTotal - discount, discount)
        } else {
            String.format(Locale.getDefault(), "Total: R %.2f", cartTotal)
        }
    }

    private fun Throwable.serverMessage(): String? {
        val body = (this as? HttpException)?.response()?.errorBody()?.string() ?: return null
        return runCatching {
            val json = JSONObject(body)
            json.optString("message").ifBlank { json.optString("detail") }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

}
