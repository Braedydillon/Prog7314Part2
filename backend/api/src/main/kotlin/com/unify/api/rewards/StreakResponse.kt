package com.unify.api.rewards

import java.math.BigDecimal
import java.time.LocalDate

data class StreakStatusResponse(
    val currentStreak: Int,
    val longestStreak: Int,
    val checkedInToday: Boolean,
    val pointsBalance: Int,
    val balanceInRand: BigDecimal,
    val shieldAvailable: Boolean,
    val shieldAvailableOn: LocalDate?,
    val nextMilestoneDay: Int?,
    val nextMilestoneBonus: Int?
)

data class CheckInResponse(
    val type: CheckInType,
    val newStreak: Int,
    val previousStreak: Int,
    val pointsEarned: Int,
    val milestoneBonus: Int,
    val status: StreakStatusResponse
)
