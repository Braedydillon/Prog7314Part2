package com.unify.api.orderitem

import java.math.BigDecimal

data class OrderItemResponse (

    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

fun OrderItem.toResponse(): OrderItemResponse = OrderItemResponse(
    id = this.id!!,
    productId = this.product?.id!!,
    productName = this.product?.productName!!,
    quantity = this.quantity ?: 0,
    unitPrice = this.unitPrice!!,
    subtotal = this.subtotal!!
)