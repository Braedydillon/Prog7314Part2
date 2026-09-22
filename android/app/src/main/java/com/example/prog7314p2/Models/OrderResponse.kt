package com.example.prog7314p2.Models

data class OrderResponse(
    val id: Int,
    val userId: String,
    val status: String,
    val items: List<OrderItemResponse>,
    val total: Double,
    val estimatedDelivery: String?,
    val createdAt: String,
    val updatedAt: String?
)

data class OrderItemResponse(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)