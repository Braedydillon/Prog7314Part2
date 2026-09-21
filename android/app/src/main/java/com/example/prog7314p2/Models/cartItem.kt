package com.example.prog7314p2.Models

data class cartItem(
    val productId: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String? = null
)