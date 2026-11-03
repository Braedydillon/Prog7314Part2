package com.unify.api.rewards

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PointTransactionRepository : JpaRepository<PointTransaction, Long> {
    fun findTop50ByUserIdOrderByCreatedAtDesc(userId: String): List<PointTransaction>
    fun findByOrderIdAndReason(orderId: Int, reason: PointReason): PointTransaction?
    fun existsByOrderIdAndReason(orderId: Int, reason: PointReason): Boolean

    @Query("""
        SELECT t.createdAt FROM PointTransaction t
        WHERE t.userId = :userId
          AND t.reason = :reason
          AND t.createdAt >= :from
          AND t.createdAt < :to
    """)
    fun findTimesBetween(
        @Param("userId") userId: String,
        @Param("reason") reason: PointReason,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): List<LocalDateTime>
}
