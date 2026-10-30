package com.unify.api.rewards

import jakarta.persistence.*
import java.time.LocalDateTime

enum class PointReason {
    DAILY_CHECK_IN,
    MILESTONE_BONUS,
    ORDER_REDEMPTION,
    ORDER_REFUND
}

@Entity
@Table(name = "point_transaction")
class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "user_id", nullable = false)
    var userId: String? = null

    @Column(name = "amount", nullable = false)
    var amount: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    var reason: PointReason? = null

    @Column(name = "order_id")
    var orderId: Int? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null
}
