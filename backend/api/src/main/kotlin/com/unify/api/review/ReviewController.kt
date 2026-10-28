package com.unify.api.review

import com.unify.api.product.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/review")
class ReviewController(val reviewRepository: ReviewRepository, val productRepository: ProductRepository) {

    @GetMapping("/product/{productId}")
    fun getReviewsForProduct(@PathVariable("productId") productId: Int): List<ReviewResponse>{
        return reviewRepository.findReviewByProductId(productId).map { it.toRespones() }
    }

    @PostMapping
    fun createReview
                (authentication: Authentication,
                 @RequestBody request: CreateReviewRequest
    ): ResponseEntity<ReviewResponse> {
        if(request.rating !in 1..5){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5")
        }
        val product = productRepository.findById(request.productId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product ${request.productId} not found") }

        if (reviewRepository.existsByUserIdAndProductId(authentication.name, request.productId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A review for this product under your name already exists")
        }

        val review = Review().apply {
            this.userId = authentication.name
            this.rating = request.rating
            this.comment = request.comment
            this.createdAt = LocalDateTime.now()
            this.product = product
        }

        val saved = reviewRepository.save(review)
        return ResponseEntity.ok(saved.toRespones())

    }

    @DeleteMapping("{/id}")
    fun deleteReview(authentication: Authentication, @PathVariable("id") id: Int): ResponseEntity<Void> {
        val review = reviewRepository.findById(id).orElse(null)
        ?: return ResponseEntity.notFound().build()

        if (review.userId != authentication.name){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Review $id not found")
        }
        reviewRepository.delete(review)
        return ResponseEntity.noContent().build()
    }
}