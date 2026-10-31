package com.example.prog7314p2.Models

enum class CheckInType { FIRST_EVER, ALREADY_CHECKED_IN, CONTINUED, SHIELD_USED, RESET }

data class StreakStatus(
    val currentStreak: Int,
    val longestStreak: Int,
    val checkedInToday: Boolean,
    val pointsBalance: Int,
    val balanceInRand: Double,
    val shieldAvailable: Boolean,
    val shieldAvailableOn: String?,
    val nextMilestoneDay: Int?,
    val nextMilestoneBonus: Int?
)

data class CheckInResponse(
    val type: CheckInType?,
    val newStreak: Int,
    val previousStreak: Int,
    val pointsEarned: Int,
    val milestoneBonus: Int,
    val status: StreakStatus
)

data class PointTransaction(
    val id: Long,
    val amount: Int,
    val reason: String,
    val orderId: Int?,
    val createdAt: String
)
