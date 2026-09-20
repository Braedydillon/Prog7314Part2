package com.unify.api.orders

import com.unify.api.orderitem.OrderItemResponse
import com.unify.api.orderitem.toResponse
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderResponse(
    val id: Int,
    val status: String,
    val items: List<OrderItemResponse>,
    val total: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?

    )

fun Orders.toResponse(): OrderResponse = OrderResponse(
    id = this.id!!,
    status = this.orderStatus ?: "",
    items = this.items.map { it.toResponse() },
    total = this.totalAmount!!,
    createdAt = this.createdAt!!,
    updatedAt = this.updatedAt!!,
)