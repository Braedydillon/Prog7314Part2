package com.unify.api.product

import java.math.BigDecimal

data class ProductResponse(
    val id: Int,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val discount: BigDecimal?,
    val imageUrl: String,
    val brand: String?,
    val stock: Int,
    val available: Boolean,
    val category: CategorySummary
    )

fun Product.toResponse(): ProductResponse = ProductResponse(
    id = this.id!!,
    name = this.productName ?: "",
    description = this.description ?: "",
    price = this.price ?: BigDecimal.ZERO,
    discount = this.discountPrice,
    stock = this.stockQuantity ?: 0,
    imageUrl = this.imageUrl ?: "",
    brand = this.brand,
    available = this.isAvailable ?: false,
    category = CategorySummary(
        id = this.category?.id ?: 0,
        name = this.category?.name ?: ""
    )
)
