package com.unify.api.review

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewRepository: JpaRepository<Review, Int>{
    fun findReviewByProductIdOrderByCreatedAtDesc(productId: Int): List<Review>
    fun existsByUserIdAndProductId(userId: String, productId: Int): Boolean
    fun countByProductId(productId: Int): Long

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product_id = :productId")
    fun averageRatingForProduct(@Param("productId") productId: Int): Double?
}

