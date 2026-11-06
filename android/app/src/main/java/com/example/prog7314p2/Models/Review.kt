package com.example.prog7314p2.Models

data class Review(
    val id: Int,
    val productId: Int,
    val userId: String,
    val reviewerName: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String?
)

data class CreateReviewRequest(
    val productId: Int,
    val rating: Int,
    val comment: String?,
    val reviewerName: String?
)

data class ReviewSummary(
    val productId: Int,
    val average: Double,
    val count: Long
)
