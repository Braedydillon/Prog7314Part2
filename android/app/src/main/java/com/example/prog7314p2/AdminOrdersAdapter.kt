package com.example.prog7314p2

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.OrderResponse
import java.util.Locale

class AdminOrdersAdapter(
    private var orders: MutableList<OrderResponse>,
    private val onChangeStatusClick: (OrderResponse) -> Unit
) : RecyclerView.Adapter<AdminOrdersAdapter.AdminOrderViewHolder>() {

    class AdminOrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvAdminOrderId)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvAdminOrderStatus)
        val tvCustomerId: TextView = view.findViewById(R.id.tvAdminCustomerId)
        val tvOrderDate: TextView = view.findViewById(R.id.tvAdminOrderDate)
        val tvOrderEta: TextView = view.findViewById(R.id.tvAdminOrderEta)
        val tvOrderItems: TextView = view.findViewById(R.id.tvAdminOrderItems)
        val tvOrderPoints: TextView = view.findViewById(R.id.tvAdminOrderPoints)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvAdminOrderTotal)
        val btnChangeStatus: Button = view.findViewById(R.id.btnChangeStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return AdminOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Order #${order.id}"
        holder.tvCustomerId.text = "Customer ID: ${order.userId ?: "N/A"}"
        holder.tvOrderDate.text = "Placed on: ${order.createdAt ?: "N/A"}"
        holder.tvOrderEta.text = "Est. Delivery: ${order.estimatedDelivery ?: "3-5 Business Days"}"

        val itemsList = order.items.orEmpty()
        val itemsText = if (itemsList.isNotEmpty()) {
            itemsList.joinToString("\n") { item ->
                val pName = item.productName ?: "Product #${item.productId ?: 0}"
                val qty = item.quantity ?: 1
                val uPrice = item.unitPrice ?: 0.0
                val subtotal = item.subtotal ?: (uPrice * qty)
                "• $pName ($qty x R ${String.format(Locale.getDefault(), "%.2f", uPrice)} = R ${String.format(Locale.getDefault(), "%.2f", subtotal)})"
            }
        } else {
            "No items listed"
        }
        holder.tvOrderItems.text = itemsText

        val pointsSpent = order.pointsSpent ?: 0
        val pointsDiscount = order.pointsDiscount ?: 0.0
        if (pointsSpent > 0) {
            holder.tvOrderPoints.visibility = View.VISIBLE
            holder.tvOrderPoints.text = String.format(
                Locale.getDefault(),
                "Points spent: %d (R %.2f off)",
                pointsSpent,
                pointsDiscount
            )
        } else {
            holder.tvOrderPoints.visibility = View.GONE
        }

        val total = order.total ?: 0.0
        holder.tvOrderTotal.text = String.format(Locale.getDefault(), "Total: R %.2f", total)

        // Status badge styling
        val currentStatus = order.status ?: "PENDING"
        val statusUpper = currentStatus.uppercase(Locale.getDefault())
        holder.tvOrderStatus.text = statusUpper
        when (statusUpper) {
            "PENDING" -> {
                holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF8E1"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#F57F17"))
            }
            "PROCESSING" -> {
                holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1565C0"))
            }
            "SHIPPED" -> {
                holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F3E5F5"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#7B1FA2"))
            }
            "DELIVERED" -> {
                holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"))
            }
            "CANCELLED" -> {
                holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#C62828"))
            }
            else -> {
                holder.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
                holder.tvOrderStatus.setTextColor(Color.parseColor("#424242"))
            }
        }

        holder.btnChangeStatus.setOnClickListener {
            onChangeStatusClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<OrderResponse>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        val index = orders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val updatedOrder = orders[index].copy(status = newStatus)
            orders[index] = updatedOrder
            notifyItemChanged(index)
        }
    }
}
