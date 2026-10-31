package com.unify.api.rewards

import java.time.LocalDate

enum class CheckInType {
    FIRST_EVER,
    ALREADY_CHECKED_IN,
    CONTINUED,
    SHIELD_USED,
    RESET
}

data class CheckInOutcome(
    val type: CheckInType,
    val newStreak: Int,
    val previousStreak: Int,
    val dailyPoints: Int,
    val milestoneBonus: Int,
    val milestoneDay: Int?,
    val shieldUsedThisCheckIn: Boolean
) {
    val totalPoints: Int
        get() = dailyPoints + milestoneBonus
}

object StreakRules {

    fun evaluate(
        currentStreak: Int,
        lastCheckIn: LocalDate?,
        lastShieldUsed: LocalDate?,
        today: LocalDate,
        props: RewardsProperties

    ): CheckInOutcome {
        if (lastCheckIn != null && !lastCheckIn.isBefore(today)) {
            return CheckInOutcome(
                type = CheckInType.ALREADY_CHECKED_IN,
                newStreak = currentStreak,
                previousStreak = currentStreak,
                dailyPoints = 0,
                milestoneBonus = 0,
                milestoneDay = null,
                shieldUsedThisCheckIn = false
            )
        }

        val type = when (lastCheckIn) {
            null -> CheckInType.FIRST_EVER
            today.minusDays(1) -> CheckInType.CONTINUED
            today.minusDays(2) if isShieldAvailable(lastShieldUsed, today, props) ->
                CheckInType.SHIELD_USED

            else -> CheckInType.RESET
        }

        val newStreak = when (type) {
            CheckInType.CONTINUED, CheckInType.SHIELD_USED -> currentStreak + 1
            else -> 1
        }

        val day = cycleDay(newStreak, props.milestoneCycleDays)
        val bonus = props.milestones[day] ?: 0

        return CheckInOutcome(
            type = type,
            newStreak = newStreak,
            previousStreak = currentStreak,
            dailyPoints = props.dailyPoints,
            milestoneBonus = bonus,
            milestoneDay = if (bonus > 0) day else null,
            shieldUsedThisCheckIn = type == CheckInType.SHIELD_USED
        )
    }

    fun isShieldAvailable(lastShieldUsed: LocalDate?, today: LocalDate, props: RewardsProperties): Boolean =
        lastShieldUsed == null ||
                !today.isBefore(lastShieldUsed.plusDays(props.shieldCooldownDays.toLong()))

    fun cycleDay(streak: Int, cycleDays: Int): Int = ((streak - 1) % cycleDays) + 1

    fun liveStreak(
        currentStreak: Int,
        lastCheckIn: LocalDate?,
        lastShieldUsed: LocalDate?,
        today: LocalDate,
        props: RewardsProperties
    ): Int = when (evaluate(currentStreak, lastCheckIn, lastShieldUsed, today, props).type) {
        CheckInType.FIRST_EVER, CheckInType.RESET -> 0
        else -> currentStreak
    }

    fun shieldOffCooldown(lastShieldUsed: LocalDate?, props: RewardsProperties): LocalDate? =
        lastShieldUsed?.plusDays(props.shieldCooldownDays.toLong())

    fun nextMilestone(streak: Int, props: RewardsProperties): Pair<Int, Int>? =
        ((streak + 1)..(streak + props.milestoneCycleDays))
            .firstOrNull { props.milestones.containsKey(cycleDay(it, props.milestoneCycleDays)) }
            ?.let { day -> day to props.milestones.getValue(cycleDay(day, props.milestoneCycleDays)) }
}
