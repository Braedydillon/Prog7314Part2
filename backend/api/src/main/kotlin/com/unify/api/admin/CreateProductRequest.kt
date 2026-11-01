package com.unify.api.admin

import java.math.BigDecimal

data class CreateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val discountPrice: BigDecimal? = null,
    val stock: Int? = null,
    val brand: String? = null,
    val categoryId: Int? = null
)
