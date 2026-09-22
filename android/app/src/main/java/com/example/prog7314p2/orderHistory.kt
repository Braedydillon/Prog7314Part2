package com.example.prog7314p2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.OrderHistoryModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class orderHistory : Fragment() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var orderAdapter: OrderHistoryAdapter
    private val ordersList = mutableListOf<OrderHistoryModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvOrderHistory = view.findViewById(R.id.rvOrderHistory)
        orderAdapter = OrderHistoryAdapter(ordersList)
        rvOrderHistory.adapter = orderAdapter

        fetchOrderHistory()
    }

    private fun fetchOrderHistory() {
        lifecycleScope.launch {
            try {
                val orders = RetrofitClient.instance.getOrders()

                if (!isAdded || view == null){
                    return@launch
                }

                val mapped = orders.map{ order ->
                    OrderHistoryModel(
                        orderId = order.id.toString(),
                        datePlaced = order.createdAt,
                        totalCost = order.total,
                        eta = order.estimatedDelivery ?: "3-5 Business Days",
                        itemCount = order.items.sumOf { it.quantity },
                        status = order.status
                    )
                }

                ordersList.clear()
                ordersList.addAll(mapped)
                orderAdapter.updateData(ordersList)

                // Save Orders to Local Offline Storage (Requirement 6.3 - Fault Tolerance)
                saveOrdersToLocalCache(mapped)

            } catch (e: Exception) {
                if (!isAdded) return@launch
                e.printStackTrace()
                // OFFLINE BACKUP: Load cached orders if network is unavailable
                loadOrdersFromLocalCache()
            }
        }
    }

    private fun saveOrdersToLocalCache(orders: List<OrderHistoryModel>) {
        try {
            val json = Gson().toJson(orders)
            requireContext().getSharedPreferences("OfflineCache", Context.MODE_PRIVATE)
                .edit().putString("CACHED_ORDERS", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadOrdersFromLocalCache() {
        try {
            val prefs = context?.getSharedPreferences("OfflineCache", Context.MODE_PRIVATE)
            val json = prefs?.getString("CACHED_ORDERS", null)

            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<List<OrderHistoryModel>>() {}.type
                val cachedOrders: List<OrderHistoryModel> = Gson().fromJson(json, type)
                ordersList.clear()
                ordersList.addAll(cachedOrders)
                orderAdapter.updateData(ordersList)
                Toast.makeText(context, "Offline Mode: Displaying cached order history", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to load order history", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}