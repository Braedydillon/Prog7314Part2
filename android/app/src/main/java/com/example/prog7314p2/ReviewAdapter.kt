package com.example.prog7314p2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7314p2.Models.Review
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var reviews: List<Review> = emptyList()
    private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvReviewerName)
        val date: TextView = view.findViewById(R.id.tvReviewDate)
        val rating: RatingBar = view.findViewById(R.id.rbReviewRating)
        val comment: TextView = view.findViewById(R.id.tvReviewComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder =
        ReviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false))

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val context = holder.itemView.context

        holder.name.text = review.reviewerName?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.review_anonymous)
        holder.rating.rating = review.rating.toFloat()
        holder.date.text = formatDate(review.createdAt)

        val comment = review.comment?.trim().orEmpty()
        holder.comment.text = comment
        holder.comment.visibility = if (comment.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = reviews.size

    fun submit(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    private fun formatDate(raw: String?): String = try {
        if (raw == null) "" else LocalDateTime.parse(raw).format(dateFormat)
    } catch (e: Exception) {
        raw?.take(10).orEmpty()
    }
}
