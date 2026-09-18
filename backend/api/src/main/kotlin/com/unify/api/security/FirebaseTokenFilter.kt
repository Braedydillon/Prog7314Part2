package com.unify.api.security

import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseAuthException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail

@Component
class FirebaseTokenFilter(private val firebaseAuth: FirebaseAuth,
                          private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        const val AUTHORIZATION_HEADER = "Authorization"
    }

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, filterChain: FilterChain) {
        val authHeader = req.getHeader(AUTHORIZATION_HEADER)

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            val token = authHeader.removePrefix(BEARER_PREFIX)
            val userId = extractUserIdFromToken(token)

            if (userId != null) {
                val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            }else {
                setAuthErrorDetails(res)
                return
            }
        }
        filterChain.doFilter(req, res)
    }

    private fun extractUserIdFromToken(token: String): String? {
        return try{
            val firebaseToken = firebaseAuth.verifyIdToken(token, true)
            firebaseToken.uid
        } catch (exception: FirebaseAuthException){
            null
        }
    }

    private fun setAuthErrorDetails(response: HttpServletResponse){
        val unauthorized = HttpStatus.UNAUTHORIZED
        response.status = unauthorized.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val problemDetail = ProblemDetail.forStatusAndDetail(
            unauthorized,
            "Failed to authenticate. Firebase token missing or expired"
        )
        response.writer.write(objectMapper.writeValueAsString(problemDetail))
    }
}