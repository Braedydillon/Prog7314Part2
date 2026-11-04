
package com.unify.api.notifications

import org.springframework.data.jpa.repository.JpaRepository

interface FcmTokenRepository : JpaRepository<FcmDeviceToken, Long> {

    fun findByUserId(userId: String): List<FcmDeviceToken>

    fun findByToken(token: String): FcmDeviceToken?
}
