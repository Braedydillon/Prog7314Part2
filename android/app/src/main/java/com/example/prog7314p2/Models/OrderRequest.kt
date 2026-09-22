package com.example.prog7314p2.Models


data class OrderRequest(
    val items: List<OrderItemRequest>,
    val addressId: Int,
)
