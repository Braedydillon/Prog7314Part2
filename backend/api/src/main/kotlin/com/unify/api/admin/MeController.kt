package com.unify.api.admin

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class MeResponse(val uid: String, val admin: Boolean)

@RestController
@RequestMapping("/api/me")
class MeController {

    @GetMapping
    fun me(authentication: Authentication): MeResponse = MeResponse(
        uid = authentication.name,
        admin = authentication.authorities.any { it.authority == AdminAccess.ROLE_ADMIN }
    )
}
