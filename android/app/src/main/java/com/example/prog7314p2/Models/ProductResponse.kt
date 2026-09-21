package com.example.prog7314p2.Models

data class ProductResponse(
    val page: Int,
    val totalPages: Int,
    val products: List<Product>
)