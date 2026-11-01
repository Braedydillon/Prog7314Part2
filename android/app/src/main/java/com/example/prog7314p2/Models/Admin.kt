package com.example.prog7314p2.Models

import java.math.BigDecimal

data class MeResponse(
    val uid: String,
    val admin: Boolean
)

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val discountPrice: BigDecimal?,
    val stock: Int,
    val brand: String?,
    val categoryId: Int
)
