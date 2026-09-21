package com.example.prog7314p2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.prog7314p2.Models.cartItem
import java.util.Locale

class CartAdapter(
    private var cartItems: List<cartItem>,
    private val onRemoveClick: (cartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivCartImage)
        val tvName: TextView = view.findViewById(R.id.tvCartName)
        val tvPrice: TextView = view.findViewById(R.id.tvCartPrice)
        val tvQty: TextView = view.findViewById(R.id.tvCartQuantity)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = String.format(Locale.getDefault(), "R %.2f", item.price)
        holder.tvQty.text = "Qty: ${item.quantity}"

        if (!item.imageUrl.isNullOrEmpty() && item.imageUrl != "no-image-yet.com") {
            holder.ivImage.load(item.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnRemove.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newItems: List<cartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}