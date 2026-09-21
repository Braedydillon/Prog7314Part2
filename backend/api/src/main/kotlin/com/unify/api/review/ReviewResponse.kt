package com.unify.api.review

import java.time.LocalDateTime

data class ReviewResponse (
    val id: Int,
    val productId: Int,
    val userId: String,
    val rating: Int,
    val comment: String,
    val createdAt: LocalDateTime
)

data class CreateReviewRequest(
    val productId: Int,
    val rating: Int,
    val comment: String?
)

fun Review.toRespones(): ReviewResponse = ReviewResponse(
    id = this.id!!,
    productId = this.product?.id!!,
    userId = this.userId!!,
    rating = this.rating!!,
    comment = this.comment ?: "",
    createdAt = this.createdAt!!
)


