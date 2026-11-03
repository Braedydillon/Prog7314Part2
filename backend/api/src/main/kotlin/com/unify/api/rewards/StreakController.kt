package com.unify.api.rewards

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/streak")
class StreakController(private val streakService: StreakService) {

    @GetMapping
    fun getStatus(authentication: Authentication): StreakStatusResponse =
        streakService.getStatus(authentication.name)

    @PostMapping("/checkin")
    fun checkIn(authentication: Authentication): CheckInResponse {
        val result = try {
            streakService.checkIn(authentication.name)
        } catch (e: DataIntegrityViolationException) {
            streakService.checkIn(authentication.name)
        }

        val outcome = result.outcome
        return CheckInResponse(
            type = outcome.type,
            newStreak = outcome.newStreak,
            previousStreak = outcome.previousStreak,
            pointsEarned = outcome.totalPoints,
            milestoneBonus = outcome.milestoneBonus,
            status = streakService.toStatus(result.streak)
        )
    }
}
