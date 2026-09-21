package com.example.prog7314p2.Models

data class OrderHistoryModel(
    val orderId: String = "",
    val datePlaced: String = "",
    val totalCost: Double = 0.0,
    val eta: String = "3-5 Business Days",
    val itemCount: Int = 0,
    val status: String = "Processing"
)