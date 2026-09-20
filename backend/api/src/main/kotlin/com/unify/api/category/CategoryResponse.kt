package com.unify.api.category

data class CategoryResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val active: Boolean
)

fun Category.toResponse(): CategoryResponse = CategoryResponse(
    id = this.id!!,
    name = this.name ?: "",
    description = this.description,
    active = this.isActive ?: false
)
