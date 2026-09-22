package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.OrderHistoryModel
import com.example.prog7314p2.Firestore.FirebaseHelper
import com.google.firebase.firestore.ListenerRegistration

class orderHistory : Fragment() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var orderAdapter: OrderHistoryAdapter
    private val ordersList = mutableListOf<OrderHistoryModel>()
    private val firebaseHelper = FirebaseHelper()


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

    private var ordersListener: ListenerRegistration? = null

    private fun fetchOrderHistory() {

        ordersListener?.remove()

        ordersListener = firebaseHelper.listenToOrderHistory(

            onUpdate = { orders ->

                if (!isAdded || view == null) {
                    return@listenToOrderHistory
                }

                ordersList.clear()
                ordersList.addAll(orders)

                orderAdapter.notifyDataSetChanged()
            },

            onFailure = { exception ->

                if (!isAdded) {
                    return@listenToOrderHistory
                }

                Toast.makeText(context,exception.message ?: "Failed to load order history",Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersListener?.remove()
    }
}