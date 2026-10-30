package com.unify.api.rewards

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "user_streak")
class UserStreak {

    @Id
    @Column(name = "user_id")
    var userId: String? = null

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int = 0

    @Column(name = "longest_streak", nullable = false)
    var longestStreak: Int = 0

    @Column(name = "last_check_in")
    var lastCheckIn: LocalDate? = null

    @Column(name = "last_shield_used")
    var lastShieldUsed: LocalDate? = null

    @Column(name = "points_balance", nullable = false)
    var pointsBalance: Int = 0
}
