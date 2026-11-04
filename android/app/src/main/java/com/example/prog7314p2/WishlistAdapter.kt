
package com.example.prog7314p2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.prog7314p2.Firestore.WishlistItem
import java.util.Locale

class WishlistAdapter(
    private val onRemoveClick: (WishlistItem) -> Unit,
    private val onProductClick: (WishlistItem) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    private val items = mutableListOf<WishlistItem>()

    class WishlistViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.ivWishlistImage)
        val name: TextView = view.findViewById(R.id.tvWishlistName)
        val price: TextView = view.findViewById(R.id.tvWishlistPrice)
        val remove: ImageButton = view.findViewById(R.id.btnRemoveWishlist)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist, parent, false)

        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WishlistViewHolder,
        position: Int
    ) {
        val item = items[position]

        holder.name.text = item.name.ifBlank {
            "Product #${item.productId}"
        }

        holder.price.text = if (item.name.isBlank()) {
            "Price unavailable"
        } else {
            String.format(Locale.getDefault(), "R %.2f", item.price)
        }

        if (!item.imageUrl.isNullOrBlank()
            && item.imageUrl != "no-image-yet.com"
        ) {
            holder.image.load(item.imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        holder.remove.setOnClickListener {
            onRemoveClick(item)
        }

        holder.itemView.setOnClickListener {
            onProductClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<WishlistItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
