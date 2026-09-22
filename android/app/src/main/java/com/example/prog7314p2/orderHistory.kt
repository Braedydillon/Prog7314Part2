package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.OrderHistoryModel
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
            } catch (e: Exception){
                if (!isAdded) return@launch
                Toast.makeText(context, e.message ?: "Failed to load order history", Toast.LENGTH_SHORT).show()
            }
        }
    }

}