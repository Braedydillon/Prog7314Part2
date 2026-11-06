package com.unify.api.review

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ReviewInput {

    const val MAX_NAME = 60
    const val MAX_COMMENT = 1000

    fun cleanName(raw: String?): String? =
        raw?.trim()?.take(MAX_NAME)?.trim()?.ifEmpty { null }

    fun cleanComment(raw: String?): String? {
        val comment = raw?.trim()?.ifEmpty { null } ?: return null
        if (comment.length > MAX_COMMENT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must be $MAX_COMMENT characters or less")
        }
        return comment
    }
}
