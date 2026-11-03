package com.unify.api.rewards

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/points")
class PointsController(private val pointsService: PointsService) {

    @GetMapping("/transactions")
    fun history(authentication: Authentication): List<PointTransactionResponse> =
        pointsService.history(authentication.name).map { it.toResponse() }
}
