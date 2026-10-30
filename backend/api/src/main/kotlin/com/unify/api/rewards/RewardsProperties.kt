package com.unify.api.rewards

import org.springframework.boot.context.properties.ConfigurationProperties
import java.math.BigDecimal
import java.math.RoundingMode

@ConfigurationProperties(prefix = "rewards")
data class RewardsProperties(
    val pointsPerRand: Int = 10,
    val dailyPoints: Int = 10,
    val milestoneCycleDays: Int = 30,
    val milestones: Map<Int, Int> = mapOf(3 to 25, 7 to 75, 14 to 150, 30 to 400),
    val shieldCooldownDays: Int = 7
) {
    init {
        require(pointsPerRand > 0) { "rewards.points-per-rand must be greater than 0" }
        require(dailyPoints >= 0) { "rewards.daily-points cant be negative" }
        require(milestoneCycleDays > 0) { "rewards.milestone-cycle-days must be greater than 0" }
        require(milestones.keys.all { it in 1..milestoneCycleDays }) {
            "Every milestone day must be between 1 and rewards.milestone-cycle-days"
        }
        require(shieldCooldownDays > 0) { "rewards.shield-cooldown-days must be greater than 0" }
    }

    fun pointsToRand(points: Int): BigDecimal =
        BigDecimal(points).divide(BigDecimal(pointsPerRand), 2, RoundingMode.DOWN)
}
