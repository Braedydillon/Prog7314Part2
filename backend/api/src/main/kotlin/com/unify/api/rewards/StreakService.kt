package com.unify.api.rewards

import org.springframework.data.repository.findByIdOrNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Clock
import java.time.temporal.TemporalAdjusters
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
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

    @Transactional(readOnly = true)
    fun getStatus(userId: String): StreakStatusResponse {
        val streak = streakRepository.findByIdOrNull(userId)
            ?: UserStreak().apply { this.userId = userId }   // never checked in: all zeros
        return toStatus(streak)
    }

    fun toStatus(streak: UserStreak): StreakStatusResponse {
        val today = LocalDate.now(clock)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val nextWeekStart = weekStart.plusDays(7)

        val checkInsThisWeek = transactionRepository.findTimesBetween(
            userId = streak.userId!!,
            reason = PointReason.DAILY_CHECK_IN,
            from = weekStart.atStartOfDay(),
            to = nextWeekStart.atStartOfDay()
        ).map { it.toLocalDate() }.distinct().sorted()

        val shieldSavedDay = streak.lastShieldUsed?.minusDays(1)
            ?.takeIf { !it.isBefore(weekStart) && it.isBefore(nextWeekStart) }

        val live = StreakRules.liveStreak(streak.currentStreak, streak.lastCheckIn, streak.lastShieldUsed, today, props)
        val shieldAvailable = StreakRules.isShieldAvailable(streak.lastShieldUsed, today, props)
        val next = StreakRules.nextMilestone(live, props)

        return StreakStatusResponse(
            currentStreak = live,
            longestStreak = streak.longestStreak,
            checkedInToday = streak.lastCheckIn == today,
            pointsBalance = streak.pointsBalance,
            balanceInRand = props.pointsToRand(streak.pointsBalance),
            shieldAvailable = shieldAvailable,
            shieldAvailableOn = if (shieldAvailable) null else StreakRules.shieldOffCooldown(streak.lastShieldUsed, props),
            nextMilestoneDay = next?.first,
            nextMilestoneBonus = next?.second,
            weekStart = weekStart,
            checkInsThisWeek = checkInsThisWeek,
            shieldSavedDay = shieldSavedDay
        )
    }
}
