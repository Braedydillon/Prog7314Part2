package com.example.prog7314p2.Models

data class UpdateOrderStatusRequest(
    val status: String,
    val orderStatus: String = status,
    val newStatus: String = status,
    val state: String = status
)
