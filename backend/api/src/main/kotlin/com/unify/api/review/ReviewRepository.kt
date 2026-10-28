package com.unify.api.review

import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository: JpaRepository<Review, Int>{
    fun findReviewByProductId(productId: Int): List<Review>
    fun existsByUserIdAndProductId(userId: String, productId: Int): Boolean
}

