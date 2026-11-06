
package com.example.prog7314p2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Firestore.FirebaseHelper
import com.example.prog7314p2.Firestore.WishlistItem
import com.google.firebase.firestore.ListenerRegistration

class WishlistFragment : Fragment() {

    private val firebaseHelper = FirebaseHelper()
    private var wishlistListener: ListenerRegistration? = null

    private lateinit var adapter: WishlistAdapter
    private lateinit var emptyText: TextView
    private lateinit var countText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_wishlist,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        emptyText = view.findViewById(R.id.tvWishlistEmpty)
        countText = view.findViewById(R.id.tvWishlistCount)

        adapter = WishlistAdapter(
            onRemoveClick = { item ->
                removeWishlistItem(item.productId)
            },
            onProductClick = { item ->
                Toast.makeText(
                    requireContext(),
                    "Product #${item.productId}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        view.findViewById<RecyclerView>(R.id.rvWishlist).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@WishlistFragment.adapter
        }

        loadWishlist()
    }

    private fun loadWishlist() {
        wishlistListener?.remove()

        wishlistListener = firebaseHelper.listenToWishlist(
            onUpdate = { items, pendingWrites ->
                if (view == null) return@listenToWishlist

                adapter.updateData(items)
                emptyText.visibility =
                    if (items.isEmpty()) View.VISIBLE else View.GONE

                countText.text = "${items.size} saved items"

                if (pendingWrites) {
                    Log.d("OfflineWishlist", "Changes pending sync")
                } else {
                    Log.d("OfflineWishlist", "No pending writes")
                }
            },
            onFailure = { error ->
                Log.e("OfflineWishlist", "Listener failed", error)

                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        error.message ?: "Failed to load wishlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun removeWishlistItem(productId: Int) {
        firebaseHelper.removeFromWishlist(
            productId = productId,
            onSuccess = {
                Log.d("OfflineWishlist", "Removal synchronized")
            },
            onFailure = { error ->
                Log.e("OfflineWishlist", "Removal failed", error)

                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to remove item",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    override fun onDestroyView() {
        wishlistListener?.remove()
        wishlistListener = null
        super.onDestroyView()
    }
}
