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
