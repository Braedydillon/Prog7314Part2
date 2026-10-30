package com.unify.api.rewards

import jakarta.transaction.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Clock

class StreakService (
    private val streakRepository: UserStreakRepository,
    private val transactionRepository: PointTransactionRepository,
    private val props: RewardsProperties,
    private val clock: Clock
) {

    data class CheckInResult(val outcome: CheckInOutcome, val streak: UserStreak)

    @Transactional
    fun checkIn(userId: String): CheckInResult {
        val today = LocalDate.now(clock)

        val streak = streakRepository.findByIdForUpdate(userId)
            ?: UserStreak().apply {this.userId = userId}

        val outcome = StreakRules.evaluate(
            currentStreak = streak.currentStreak,
            lastCheckIn = streak.lastCheckIn,
            lastShieldUsed = streak.lastShieldUsed,
            today = today,
            props = props
        )

        if (outcome.type == CheckInType.ALREADY_CHECKED_IN){
            return CheckInResult(outcome, streak)
        }

        streak.currentStreak = outcome.newStreak
        streak.longestStreak = maxOf(streak.longestStreak, outcome.newStreak)
        streak.lastCheckIn = today

        if (outcome.shieldUsedThisCheckIn) streak.lastShieldUsed = today

        val now = LocalDateTime.now(clock)

        addPoints(streak, outcome.dailyPoints, PointReason.DAILY_CHECK_IN, now)
        addPoints(streak, outcome.milestoneBonus, PointReason.MILESTONE_BONUS, now)

        return CheckInResult(outcome, streakRepository.save(streak))

    }

    private fun addPoints(streak: UserStreak, amount: Int, reason: PointReason, now: LocalDateTime) {
        if (amount == 0) return

        streak.pointsBalance += amount
        transactionRepository.save(PointTransaction().apply {
            this.userId = streak.userId
            this.amount = amount
            this.reason = reason
            this.createdAt = now
        })
    }
}
