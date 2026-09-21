package com.example.prog7314p2.Models

data class Product(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val discount: Double?,
    val imageUrl: String?,
    val brand: String?,
    val stock: Int,
    val available: Boolean,
    val category: Category?
)