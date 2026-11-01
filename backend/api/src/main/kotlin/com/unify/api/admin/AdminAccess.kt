package com.unify.api.admin

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class AdminAccess(private val adminUserRepository: AdminUserRepository) {

    fun authoritiesFor(userId: String): List<GrantedAuthority> =
        if (adminUserRepository.existsById(userId)) listOf(SimpleGrantedAuthority(ROLE_ADMIN))
        else emptyList()

    companion object {
        const val ROLE_ADMIN = "ROLE_ADMIN"
    }
}
