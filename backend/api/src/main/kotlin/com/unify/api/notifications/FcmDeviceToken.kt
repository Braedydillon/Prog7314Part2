
package com.unify.api.notifications

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "fcm_device_tokens")
class FcmDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "user_id", nullable = false)
    var userId: String = ""

    @Column(
        name = "token",
        nullable = false,
        unique = true,
        columnDefinition = "text"
    )
    var token: String = ""

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
}
