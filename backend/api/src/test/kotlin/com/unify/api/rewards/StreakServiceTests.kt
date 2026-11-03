package com.unify.api.rewards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StreakServiceTests {

    private val zone = ZoneId.of("Africa/Johannesburg")
    private val today = LocalDate.of(2004, 6, 19)

    private val streakRepository: UserStreakRepository = mock()
    private val transactionRepository: PointTransactionRepository = mock()
    private val props = RewardsProperties()

    private fun serviceAt(sastDateTime: LocalDateTime): StreakService {
        val instant = sastDateTime.atZone(zone).toInstant()
        return StreakService(streakRepository, transactionRepository, props, Clock.fixed(instant, zone))
    }

    private fun givenStreak(streak: UserStreak?) {
        whenever(streakRepository.findByIdForUpdate("user")).thenReturn(streak)
        whenever(streakRepository.save(any<UserStreak>())).thenAnswer { it.arguments[0] as UserStreak }
    }

    private fun existing(
        current: Int,
        lastCheckIn: LocalDate,
        lastShieldUsed: LocalDate? = null,
        balance: Int = 0
    ) = UserStreak().apply {
        userId = "user"
        currentStreak = current
        longestStreak = current
        this.lastCheckIn = lastCheckIn
        this.lastShieldUsed = lastShieldUsed
        pointsBalance = balance
    }

    @Test
    fun `first check in creates streak and gives reward`() {
        givenStreak(null)

        val result = serviceAt(today.atTime(9, 0)).checkIn("user")

        assertEquals(1, result.streak.currentStreak)
        assertEquals(10, result.streak.pointsBalance)
        assertEquals(today, result.streak.lastCheckIn)

        val ledger = argumentCaptor<PointTransaction>()
        verify(transactionRepository).save(ledger.capture())
        assertEquals(10, ledger.firstValue.amount)
        assertEquals(PointReason.DAILY_CHECK_IN, ledger.firstValue.reason)
    }

    @Test
    fun `milestone writes two transactions`() {
        givenStreak(existing(current = 6, lastCheckIn = today.minusDays(1), balance = 100))

        val result = serviceAt(today.atTime(9, 0)).checkIn("user")

        assertEquals(185, result.streak.pointsBalance)
        val ledger = argumentCaptor<PointTransaction>()
        verify(transactionRepository, times(2)).save(ledger.capture())
        assertEquals(
            listOf(PointReason.DAILY_CHECK_IN, PointReason.MILESTONE_BONUS),
            ledger.allValues.map { it.reason }
        )
    }

    @Test
    fun `reopening on same day has no change`() {
        val streak = existing(current = 5, lastCheckIn = today, lastShieldUsed = today, balance = 50)
        givenStreak(streak)

        val result = serviceAt(today.atTime(15, 0)).checkIn("user")

        assertEquals(CheckInType.ALREADY_CHECKED_IN, result.outcome.type)
        assertEquals(today, streak.lastShieldUsed) // still on cooldown
        assertEquals(50, streak.pointsBalance)
        verify(streakRepository, never()).save(any<UserStreak>())
        verify(transactionRepository, never()).save(any<PointTransaction>())
    }

    @Test
    fun `using shield records current date`() {
        givenStreak(existing(current = 4, lastCheckIn = today.minusDays(2)))

        val result = serviceAt(today.atTime(9, 0)).checkIn("user")

        assertEquals(CheckInType.SHIELD_USED, result.outcome.type)
        assertEquals(today, result.streak.lastShieldUsed)
    }

    @Test
    fun `longest streak survives a reset`() {
        givenStreak(existing(current = 12, lastCheckIn = today.minusDays(10)))

        val result = serviceAt(today.atTime(9, 0)).checkIn("user")

        assertEquals(1, result.streak.currentStreak)
        assertEquals(12, result.streak.longestStreak)
    }
}
