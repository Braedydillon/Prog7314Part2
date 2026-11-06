
package com.unify.api.notifications

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

data class RegisterFcmTokenRequest(
    val token: String
)

@RestController
@RequestMapping("/api/notifications")
class FcmTokenController(
    private val repository: FcmTokenRepository
) {

    @PostMapping("/token")
    fun registerToken(
        authentication: Authentication,
        @RequestBody request: RegisterFcmTokenRequest
    ): ResponseEntity<Void> {

        val token = request.token.trim()

        if (token.isEmpty() || token.length > 2048) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid FCM token"
            )
        }

        val device = repository.findByToken(token)
            ?: FcmDeviceToken()

        device.token = token
        device.userId = authentication.name
        device.updatedAt = Instant.now()

        repository.save(device)

        return ResponseEntity.noContent().build()
    }
}
