package com.unify.api.rewards

import java.time.LocalDateTime

data class PointTransactionResponse(
    val id: Long,
    val amount: Int,
    val reason: PointReason,
    val orderId: Int?,
    val createdAt: LocalDateTime
)

fun PointTransaction.toResponse() = PointTransactionResponse(
    id = this.id!!,
    amount = this.amount,
    reason = this.reason!!,
    orderId = this.orderId,
    createdAt = this.createdAt!!
)
