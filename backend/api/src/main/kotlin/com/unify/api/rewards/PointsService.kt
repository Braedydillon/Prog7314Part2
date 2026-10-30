package com.unify.api.rewards

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

@Service
class PointsService(
    private val streakRepository: UserStreakRepository,
    private val transactionRepository: PointTransactionRepository,
    private val props: RewardsProperties,
    private val clock: Clock
) {

    @Transactional
    fun spend(userId: String, points: Int, orderId: Int): BigDecimal {
        if (points <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to spend more than 0 points")
        }

        val updated = streakRepository.deductBalance(userId, points)
        if (updated == 0) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Not enough points")
        }

        record(userId, -points, PointReason.ORDER_REDEMPTION, orderId)
        return props.pointsToRand(points)
    }

    @Transactional
    fun refundOrder(orderId: Int): Int {
        if (transactionRepository.existsByOrderIdAndReason(orderId, PointReason.ORDER_REFUND)) return 0

        val redemption = transactionRepository.findByOrderIdAndReason(orderId, PointReason.ORDER_REDEMPTION)
            ?: return 0

        val userId = redemption.userId!!
        val points = -redemption.amount

        streakRepository.increaseBalance(userId, points)
        record(userId, points, PointReason.ORDER_REFUND, orderId)
        return points
    }

    @Transactional(readOnly = true)
    fun history(userId: String): List<PointTransaction> =
        transactionRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)

    private fun record(userId: String, amount: Int, reason: PointReason, orderId: Int?) {
        transactionRepository.save(PointTransaction().apply {
            this.userId = userId
            this.amount = amount
            this.reason = reason
            this.orderId = orderId
            this.createdAt = LocalDateTime.now(clock)
        })
    }
}
