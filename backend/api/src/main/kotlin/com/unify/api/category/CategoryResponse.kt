package com.unify.api.category

data class CategoryResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val active: Boolean
)
