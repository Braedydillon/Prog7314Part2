package com.unify.api.product

import com.unify.api.category.Category
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "product")

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    public var id: Int? = null
    @Column(name = "product_name")
    val productName: String? = null
    @Column(name = "description")
    val description: String? = null
    @Column(name = "price")
    val price: BigDecimal? = null
    @Column(name = "discount_price")
    val discountPrice: BigDecimal? = null
    @Column(name = "stock_quantity")
    var stockQuantity: Int? = null
    @Column(name = "image_url")
    val imageUrl: String? = null
    @Column(name = "brand")
    val brand: String? = null
    @Column(name = "is_available")
    val isAvailable: Boolean? = null
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null

}