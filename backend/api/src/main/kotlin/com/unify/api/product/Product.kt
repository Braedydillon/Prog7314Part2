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
    var productName: String? = null
    @Column(name = "description")
    var description: String? = null
    @Column(name = "price")
    var price: BigDecimal? = null
    @Column(name = "discount_price")
    var discountPrice: BigDecimal? = null
    @Column(name = "stock_quantity")
    var stockQuantity: Int? = null
    @Column(name = "image_url")
    var imageUrl: String? = null
    @Column(name = "brand")
    var brand: String? = null
    @Column(name = "is_available")
    var isAvailable: Boolean? = null
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null

}