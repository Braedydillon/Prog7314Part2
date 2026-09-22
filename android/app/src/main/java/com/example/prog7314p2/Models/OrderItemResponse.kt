package com.example.prog7314p2.Models

data class OrderItemResponse(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)