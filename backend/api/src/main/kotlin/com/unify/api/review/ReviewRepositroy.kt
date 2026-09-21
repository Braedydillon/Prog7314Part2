package com.unify.api.review

import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepositroy: JpaRepository<Review, Int>{
    fun findReviewByProductId(productId: Int): List<Review>
}

