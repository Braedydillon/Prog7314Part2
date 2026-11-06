package com.example.prog7314p2

import RetrofitClient
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Firestore.FirebaseHelper
import com.example.prog7314p2.Firestore.WishlistItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyText = view.findViewById(R.id.tvWishlistEmpty)
        countText = view.findViewById(R.id.tvWishlistCount)

        adapter = WishlistAdapter(
            onRemoveClick = { item ->
                removeWishlistItem(item.productId)
            },
            onProductClick = { item ->
                openProductDetails(item.productId)
            },
            onAddToCartClick = { item ->
                addToCartFromWishlist(item, view)
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
                emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
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

    private fun openProductDetails(productId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val product = RetrofitClient.instance.getProductDetails(productId)
                val detailsFragment = ProductDetailsFragment()
                val bundle = Bundle().apply {
                    putSerializable("PRODUCT_DATA", product)
                }
                detailsFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack(null)
                    .commit()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to open product: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addToCartFromWishlist(item: WishlistItem, view: View) {
        firebaseHelper.addToCart(
            productId = item.productId,
            quantity = 1,
            name = item.name,
            price = item.price,
            imageUrl = item.imageUrl,
            onSuccess = {
                if (isAdded) {
                    val pName = item.name.ifBlank { "Product #${item.productId}" }
                    Snackbar.make(view, "$pName added to cart!", Snackbar.LENGTH_LONG)
                        .setAction("GO TO CART") {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, ShoppingCart())
                                .addToBackStack(null)
                                .commit()
                        }.show()
                }
            },
            onFailure = { error ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to add to cart: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun removeWishlistItem(productId: Int) {
        firebaseHelper.removeFromWishlist(
            productId = productId,
            onSuccess = {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Item removed from wishlist", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { error ->
                Log.e("OfflineWishlist", "Removal failed", error)
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show()
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
