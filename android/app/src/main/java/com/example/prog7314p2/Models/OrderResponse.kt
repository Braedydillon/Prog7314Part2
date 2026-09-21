package com.example.prog7314p2.Models

data class OrderResponse(
    val orderId: Int,
    val status: String,
    val total: Double,
    val estimatedDelivery: String?
)