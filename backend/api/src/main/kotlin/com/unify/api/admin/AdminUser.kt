package com.unify.api.admin

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "admin_user")
class AdminUser {
    @Id
    @Column(name = "user_id")
    var userId: String? = null

    @Column(name = "note")
    var note: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null
}
