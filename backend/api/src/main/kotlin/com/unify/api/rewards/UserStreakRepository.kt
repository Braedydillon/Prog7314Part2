package com.unify.api.rewards

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserStreakRepository : JpaRepository<UserStreak, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserStreak s WHERE s.userId = :userId")
    fun findByIdForUpdate(@Param("userId") userId: String): UserStreak?

    @Modifying
    @Query("""
        UPDATE UserStreak s
        SET s.pointsBalance = s.pointsBalance - :points
        WHERE s.userId = :userId AND s.pointsBalance >= :points
    """)
    fun deductBalance(@Param("userId") userId: String, @Param("points") points: Int): Int

    @Modifying
    @Query("UPDATE UserStreak s SET s.pointsBalance = s.pointsBalance + :points WHERE s.userId = :userId")
    fun increaseBalance(@Param("userId") userId: String, @Param("points") points: Int): Int
}
