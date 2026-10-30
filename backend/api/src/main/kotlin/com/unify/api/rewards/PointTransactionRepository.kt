package com.unify.api.rewards

import org.springframework.data.jpa.repository.JpaRepository

interface PointTransactionRepository : JpaRepository<PointTransaction, Long> {
    fun findTop50ByUserIdOrderByCreatedAtDesc(userId: String): List<PointTransaction>
}
