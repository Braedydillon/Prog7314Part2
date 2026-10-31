package com.unify.api.orders

data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val addressId: Int,
    val pointsToSpend: Int = 0
)

data class OrderItemRequest(
    val productId: Int,
    val quantity: Int
)

data class UpdateOrderStatus(
    val status: String
)
