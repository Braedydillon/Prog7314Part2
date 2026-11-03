package com.example.prog7314p2

import RetrofitClient
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.prog7314p2.Firestore.FirebaseHelper
import com.example.prog7314p2.Models.CreateReviewRequest
import com.example.prog7314p2.Models.Product
import com.example.prog7314p2.util.UserProfile
import com.example.prog7314p2.util.serverMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Locale

class ProductDetailsFragment : Fragment() {

    private val firebaseHelper = FirebaseHelper()
    private val reviewAdapter = ReviewAdapter()

    private var productId: Int? = null
    private var myReviewId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvReviews = view.findViewById<RecyclerView>(R.id.rvReviews)
        rvReviews.adapter = reviewAdapter
        rvReviews.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        view.findViewById<Button>(R.id.btnSubmitReview).setOnClickListener { submitReview(view) }
        view.findViewById<Button>(R.id.btnDeleteMyReview).setOnClickListener { confirmDeleteReview(view) }

        val product = arguments?.getSerializable("PRODUCT_DATA") as? Product
        productId = product?.id

        productId?.let {
            fetchProductDetails(it, view)
            loadReviews(view)
        }
    }

    private fun fetchProductDetails(productId: Int, view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fetchedProduct = RetrofitClient.instance.getProductDetails(productId)

                val ivImage = view.findViewById<ImageView>(R.id.ivDetailImage)
                val tvName = view.findViewById<TextView>(R.id.tvDetailName)
                val tvPrice = view.findViewById<TextView>(R.id.tvDetailPrice)
                val tvDesc = view.findViewById<TextView>(R.id.tvDetailDescription)
                val btnAddCart = view.findViewById<Button>(R.id.btnAddCart)

                tvName.text = fetchedProduct.name
                tvPrice.text = String.format(Locale.getDefault(), "R %.2f", fetchedProduct.price)
                tvDesc.text = fetchedProduct.description ?: "No description available."

                val imageUrl = fetchedProduct.imageUrl
                if (!imageUrl.isNullOrEmpty() && imageUrl != "no-image-yet.com") {
                    ivImage.load(imageUrl) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                btnAddCart.setOnClickListener {
                    firebaseHelper.addToCart(
                        productId = fetchedProduct.id,
                        quantity = 1,
                        onSuccess = {
                            Snackbar.make(
                                view,
                                "${fetchedProduct.name} added to cart!",
                                Snackbar.LENGTH_LONG
                            ).setAction("GO TO CART") {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainer, ShoppingCart())
                                    .addToBackStack(null)
                                    .commit()
                            }.show()
                        },
                        onFailure = { e ->
                            Toast.makeText(context, "Failed to add to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadReviews(view: View) {
        val id = productId ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reviews = RetrofitClient.instance.getReviews(id)
                val summary = RetrofitClient.instance.getReviewSummary(id)

                reviewAdapter.submit(reviews)
                view.findViewById<View>(R.id.tvNoReviews).visibility =
                    if (reviews.isEmpty()) View.VISIBLE else View.GONE

                view.findViewById<RatingBar>(R.id.rbAverage).rating = summary.average.toFloat()
                view.findViewById<TextView>(R.id.tvRatingSummary).text =
                    if (summary.count == 0L) getString(R.string.reviews_none_yet)
                    else resources.getQuantityString(
                        R.plurals.reviews_summary, summary.count.toInt(), summary.average, summary.count.toInt()
                    )

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                myReviewId = reviews.firstOrNull { it.userId == uid }?.id
                val hasReviewed = myReviewId != null
                view.findViewById<View>(R.id.cardWriteReview).visibility = if (hasReviewed) View.GONE else View.VISIBLE
                view.findViewById<View>(R.id.cardMyReview).visibility = if (hasReviewed) View.VISIBLE else View.GONE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Reviews", "Couldn't load reviews", e)
                view.findViewById<TextView>(R.id.tvRatingSummary).text = getString(R.string.reviews_load_failed)
            }
        }
    }

    private fun submitReview(view: View) {
        val id = productId ?: return
        val ratingBar = view.findViewById<RatingBar>(R.id.rbNewRating)
        val etComment = view.findViewById<EditText>(R.id.etReviewComment)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitReview)

        val rating = ratingBar.rating.toInt()
        if (rating < 1) {
            Toast.makeText(requireContext(), R.string.review_pick_rating, Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RetrofitClient.instance.createReview(
                    CreateReviewRequest(
                        productId = id,
                        rating = rating,
                        comment = etComment.text.toString().trim().ifEmpty { null },
                        reviewerName = UserProfile.reviewerName()
                    )
                )
                ratingBar.rating = 0f
                etComment.text?.clear()
                Toast.makeText(requireContext(), R.string.review_thanks, Toast.LENGTH_SHORT).show()
                loadReviews(view)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Reviews", "Couldn't submit review", e)
                val message = e.serverMessage() ?: getString(R.string.review_submit_failed)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            } finally {
                btnSubmit.isEnabled = true
            }
        }
    }

    private fun confirmDeleteReview(view: View) {
        val reviewId = myReviewId ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.review_delete_title)
            .setMessage(R.string.review_delete_message)
            .setNegativeButton(R.string.review_cancel, null)
            .setPositiveButton(R.string.review_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        RetrofitClient.instance.deleteReview(reviewId)
                        loadReviews(view)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w("Reviews", "Couldn't delete review", e)
                        Toast.makeText(requireContext(), R.string.review_delete_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
}
