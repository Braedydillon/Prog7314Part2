package com.example.prog7314p2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.OrderHistoryModel
import java.util.Locale

class OrderHistoryAdapter(private var orders: List<OrderHistoryModel>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderEta: TextView = view.findViewById(R.id.tvOrderEta)
        val tvOrderItemsCount: TextView = view.findViewById(R.id.tvOrderItemsCount)
        val tvOrderCost: TextView = view.findViewById(R.id.tvOrderCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvOrderStatus.text = order.status
        holder.tvOrderDate.text = "Placed on: ${order.datePlaced}"
        holder.tvOrderEta.text = "Est. Delivery: ${order.eta}"
        holder.tvOrderItemsCount.text = "${order.itemCount} Item(s)"
        holder.tvOrderCost.text = String.format(Locale.getDefault(), "Total: R %.2f", order.totalCost)
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<OrderHistoryModel>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}