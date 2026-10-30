package com.unify.api.rewards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StreakRulesTests {

    private val props = RewardsProperties()
    private val today = LocalDate.of(2004, 6, 19)

    private fun checkIn(
        streak: Int = 0,
        lastCheckIn: LocalDate? = null,
        lastShieldUsed: LocalDate? = null,
        on: LocalDate = today
    ) = StreakRules.evaluate(streak, lastCheckIn, lastShieldUsed, on, props)

    @Test
    fun `first check in equals streak 1`() {
        val result = checkIn()
        assertEquals(CheckInType.FIRST_EVER, result.type)
        assertEquals(1, result.newStreak)
        assertEquals(10, result.totalPoints)
    }

    @Test
    fun `second check in on same day equals no change`() {
        val result = checkIn(streak = 4, lastCheckIn = today)
        assertEquals(CheckInType.ALREADY_CHECKED_IN, result.type)
        assertEquals(4, result.newStreak)
        assertEquals(0, result.totalPoints)
    }

    @Test
    fun `next day continues streak`() {
        val result = checkIn(streak = 4, lastCheckIn = today.minusDays(1))
        assertEquals(CheckInType.CONTINUED, result.type)
        assertEquals(5, result.newStreak)
    }

    @Test
    fun `missing a day triggers shield`() {
        val result = checkIn(streak = 4, lastCheckIn = today.minusDays(2), lastShieldUsed = null)
        assertEquals(CheckInType.SHIELD_USED, result.type)
        assertEquals(5, result.newStreak)
        assertTrue(result.shieldUsedThisCheckIn)
    }

    @Test
    fun `missing day when shield on cooldown resets`() {
        val result = checkIn(streak = 4, lastCheckIn = today.minusDays(2), lastShieldUsed = today.minusDays(3))
        assertEquals(CheckInType.RESET, result.type)
        assertEquals(1, result.newStreak)
        assertEquals(4, result.previousStreak)
    }

    @Test
    fun `shield off cooldown after 7 days`() {
        assertTrue(StreakRules.isShieldAvailable(today.minusDays(7), today, props))
        assertFalse(StreakRules.isShieldAvailable(today.minusDays(6), today, props))
    }

    @Test
    fun `missing two days resets even if shield is up`() {
        val result = checkIn(streak = 10, lastCheckIn = today.minusDays(3), lastShieldUsed = null)
        assertEquals(CheckInType.RESET, result.type)
        assertFalse(result.shieldUsedThisCheckIn)
    }

    @Test
    fun `day 7 equals milestone bonus`() {
        val result = checkIn(streak = 6, lastCheckIn = today.minusDays(1))
        assertEquals(7, result.newStreak)
        assertEquals(75, result.milestoneBonus)
        assertEquals(7, result.milestoneDay)
        assertEquals(85, result.totalPoints)
    }

    @Test
    fun `milestones repeat every 30 days`() {
        assertEquals(25, checkIn(streak = 32, lastCheckIn = today.minusDays(1)).milestoneBonus)  // day 33
        assertEquals(400, checkIn(streak = 59, lastCheckIn = today.minusDays(1)).milestoneBonus) // day 60
        assertEquals(0, checkIn(streak = 30, lastCheckIn = today.minusDays(1)).milestoneBonus)   // day 31
    }

    @Test
    fun `cycle wraps`() {
        assertEquals(1, StreakRules.cycleDay(1, 30))
        assertEquals(30, StreakRules.cycleDay(30, 30))
        assertEquals(1, StreakRules.cycleDay(31, 30))
        assertEquals(30, StreakRules.cycleDay(60, 30))
    }

}
