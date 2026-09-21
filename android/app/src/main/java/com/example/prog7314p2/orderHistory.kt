package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.OrderHistoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

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

    private var ordersListener: ListenerRegistration? = null

    private fun fetchOrderHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        ordersListener?.remove()

        ordersListener = FirebaseFirestore.getInstance()
            .collection("Users").document(userId)
            .collection("Orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                if (!isAdded || view == null) return@addSnapshotListener

                ordersList.clear()
                for (doc in snapshot.documents) {
                    val order = doc.toObject(OrderHistoryModel::class.java)
                    if (order != null) {
                        ordersList.add(order)
                    }
                }
                orderAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersListener?.remove()
    }
}