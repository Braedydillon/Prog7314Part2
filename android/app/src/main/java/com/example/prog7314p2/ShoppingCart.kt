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
import com.example.prog7314p2.util.serverMessage
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale
import com.google.firebase.firestore.ListenerRegistration
import com.example.prog7314p2.RoomDB.AppDatabase
import com.example.prog7314p2.RoomDB.OfflineCartEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class ShoppingCart : Fragment() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartItemsList = mutableListOf<cartItem>()
    private var cartTotal = 0.0
    private var streakStatus: StreakStatus? = null
    private var cartListener: ListenerRegistration? = null
    private var isCartSyncing = false
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val firebaseHelper = FirebaseHelper()

    private val roomDatabase by lazy {
        AppDatabase.getDatabase(requireContext().applicationContext)
    }

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

        loadCartFromRoom()
        loadCart()
        registerConnectivityCallback()
        syncPendingCartChanges()
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


    private fun saveCartToRoom(items: List<cartItem>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = roomDatabase

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val dao = database.offlineCartDao()

                    database.withTransaction {

                        items.forEach { item ->
                            val existing = dao.getCartItem(
                                userId,
                                item.productId
                            )

                            // Never overwrite a pending local operation.
                            if (existing?.pendingSync != true && existing?.isDeleted != true) {
                                dao.insertCartItem(
                                    OfflineCartEntity(
                                        userId = userId,
                                        productId = item.productId,
                                        name = item.name,
                                        price = item.price,
                                        quantity = item.quantity,
                                        imageUrl = item.imageUrl
                                    )
                                )
                            }
                        }
                    }
                }

                Log.d(
                    "RoomDB",
                    "Firestore cart cached without overwriting pending changes"
                )

                // Refresh the screen using the local database
                loadCartFromRoom()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RoomDB", "Failed to cache cart", e)
            }
        }
    }


    private fun loadCart() {

        cartListener?.remove()

        cartListener = firebaseHelper.listenToCart(
            onUpdate = { items, hasPendingWrites, isFromCache ->

                if (!isAdded || view == null) {
                    return@listenToCart
                }

                // Convert Firestore items to UI cart items
                val updatedItems = items.map { item ->
                    cartItem(
                        productId = item.productId,
                        name = item.name.ifBlank {
                            "Product #${item.productId}"
                        },
                        price = item.price,
                        quantity = item.quantity,
                        imageUrl = item.imageUrl
                    )
                }


                if (!isFromCache && !hasPendingWrites) {
                    saveCartToRoom(updatedItems)
                } else {
                    Log.d(
                        "RoomDB",
                        "Skipping unconfirmed Firestore snapshot"
                    )
                }

                if (hasPendingWrites) {
                    Log.d(
                        "OfflineCart",
                        "Cart changes pending synchronization"
                    )
                } else {
                    Log.d(
                        "OfflineCart",
                        "No pending local cart changes"
                    )
                }
            },

            onFailure = { exception ->
                Log.e(
                    "OfflineCart",
                    "Failed to listen to cart",
                    exception
                )
            }
        )
    }

    private fun checkRoomCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    roomDatabase.offlineCartDao().getCartItems(userId)
                }

                Log.d("RoomDB", "Stored cart items: ${items.size}")

                items.forEach { item ->
                    Log.d(
                        "RoomDB",
                        "Product: ${item.name}, Quantity: ${item.quantity}"
                    )
                }

            } catch (e: Exception) {
                Log.e("RoomDB", "Failed to read cart", e)
            }
        }
    }


    private fun displayCartItems(items: List<cartItem>) {
        cartItemsList.clear()
        cartItemsList.addAll(items)

        cartAdapter.notifyDataSetChanged()

        cartTotal = items.sumOf {
            it.price * it.quantity
        }

        updateTotal()
    }



    private fun loadCartFromRoom() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val storedItems = withContext(Dispatchers.IO) {
                    roomDatabase.offlineCartDao().getCartItems(userId)
                }

                if (!isAdded || view == null) return@launch

                val localItems = storedItems.map { item ->
                    cartItem(
                        productId = item.productId,
                        name = item.name,
                        price = item.price,
                        quantity = item.quantity,
                        imageUrl = item.imageUrl
                    )
                }

                displayCartItems(localItems)

                Log.d(
                    "RoomDB",
                    "Loaded ${localItems.size} cart items from local database"
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RoomDB", "Failed to load local cart", e)
            }
        }
    }



    private fun removeFromCart(productId: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Save the deletion locally first.
                withContext(Dispatchers.IO) {
                    roomDatabase.offlineCartDao().markItemDeleted(
                        userId = userId,
                        productId = productId
                    )
                }

                // Immediately update the UI from RoomDB.
                loadCartFromRoom()

                Log.d(
                    "RoomDB",
                    "Product $productId marked for offline deletion"
                )

                // Firestore synchronization will be handled separately.
                // Do not call firebaseHelper.removeFromCart() here yet.

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RoomDB", "Failed to remove cart item locally", e)

                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to remove item",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun syncPendingCartChanges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (!isNetworkAvailable()) {
            Log.d("RoomDB", "Offline: synchronization postponed")
            return
        }

        if (isCartSyncing) {
            Log.d("RoomDB", "Synchronization already running")
            return
        }

        isCartSyncing = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pendingItems = withContext(Dispatchers.IO) {
                    roomDatabase.offlineCartDao().getPendingSyncItems(userId)
                }

                if (pendingItems.isEmpty()) {
                    Log.d("RoomDB", "No pending cart changes")
                    return@launch
                }

                Log.d(
                    "RoomDB",
                    "Synchronizing ${pendingItems.size} pending cart changes"
                )

                for (item in pendingItems) {
                    val success = suspendCancellableCoroutine<Boolean> { continuation ->

                        if (item.isDeleted) {
                            firebaseHelper.removeFromCart(
                                productId = item.productId,
                                onSuccess = {
                                    if (continuation.isActive) {
                                        continuation.resume(true)
                                    }
                                },
                                onFailure = { exception ->
                                    Log.e("RoomDB", "Deletion sync failed", exception)
                                    if (continuation.isActive) {
                                        continuation.resume(false)
                                    }
                                }
                            )
                        } else {
                            firebaseHelper.setCartQuantityForSync(
                                productId = item.productId,
                                quantity = item.quantity,
                                onSuccess = {
                                    if (continuation.isActive) {
                                        continuation.resume(true)
                                    }
                                },
                                onFailure = { exception ->
                                    Log.e("RoomDB", "Quantity sync failed", exception)
                                    if (continuation.isActive) {
                                        continuation.resume(false)
                                    }
                                }
                            )
                        }
                    }

                    if (success) {
                        withContext(Dispatchers.IO) {
                            val dao = roomDatabase.offlineCartDao()


                            if (item.isDeleted) {
                                dao.markAsSynced(
                                    userId,
                                    item.productId,
                                    item.updatedAt
                                )
                            } else {
                                dao.markAsSynced(
                                    userId,
                                    item.productId,
                                    item.updatedAt
                                )
                            }

                        }

                        Log.d(
                            "RoomDB",
                            "Product ${item.productId} synchronized"
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("RoomDB", "Cart synchronization failed", e)
            } finally {
                isCartSyncing = false
            }
        }
    }

    private fun registerConnectivityCallback() {
        if (networkCallback != null) return

        val manager = requireContext().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        connectivityManager = manager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )

                if (hasInternet) {
                    activity?.runOnUiThread {
                        if (isAdded && view != null) {
                            Log.d(
                                "RoomDB",
                                "Internet restored: checking pending cart changes"
                            )
                            syncPendingCartChanges()
                        }
                    }
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            manager.registerNetworkCallback(request, callback)
            networkCallback = callback
        } catch (e: Exception) {
            Log.e("RoomDB", "Failed to register connectivity callback", e)
        }
    }

    override fun onDestroyView() {
        cartListener?.remove()
        cartListener = null

        networkCallback?.let { callback ->
            try {
                connectivityManager?.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Log.w("RoomDB", "Failed to unregister connectivity callback", e)
            }
        }

        networkCallback = null
        connectivityManager = null

        super.onDestroyView()
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = requireContext().getSystemService(
            android.content.Context.CONNECTIVITY_SERVICE
        ) as android.net.ConnectivityManager

        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network)
            ?: return false

        return capabilities.hasCapability(
            android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) && capabilities.hasCapability(
            android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }



    private fun processCheckout() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (!isNetworkAvailable()) {
            Toast.makeText(
                requireContext(),
                "Checkout requires an internet connection. Your cart is saved.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        lifecycleScope.launch {
            try{

                val hasPendingChanges = withContext(Dispatchers.IO) {
                    roomDatabase.offlineCartDao()
                        .getPendingSyncItems(userId)
                        .isNotEmpty()
                }

                if (hasPendingChanges || isCartSyncing) {
                    Toast.makeText(
                        requireContext(),
                        "Please wait for your cart to finish syncing before checkout.",
                        Toast.LENGTH_LONG
                    ).show()

                    syncPendingCartChanges()
                    return@launch
                }

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

                // The order was successfully created by Spring Boot.
                // Clear the local cart so purchased items do not reappear offline.
                withContext(Dispatchers.IO) {
                    roomDatabase.offlineCartDao().clearCart(userId)
                }

                Log.d("RoomDB", "Local cart cleared after successful checkout")

                // Keep the existing Firestore clearing behaviour.
                firebaseHelper.clearCart(
                    onSuccess = {
                        Log.d("RoomDB", "Firestore cart cleared after checkout")
                    },
                    onFailure = { exception ->
                        Log.e("RoomDB", "Firestore cart clearing failed", exception)
                    }
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

}
