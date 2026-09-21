package com.unify.api.review

import com.unify.api.product.Product
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "review")
class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(name = "user_id")
    var userId: String? = null
    @Column(name = "rating")
    var rating: Int? = null
    @Column(name = "comment")
    var comment: String? = null
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null

}