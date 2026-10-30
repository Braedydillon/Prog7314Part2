package com.unify.api.rewards

import org.springframework.data.jpa.repository.JpaRepository

interface UserStreakRepository : JpaRepository<UserStreak, String>
