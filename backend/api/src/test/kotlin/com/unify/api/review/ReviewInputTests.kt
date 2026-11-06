package com.unify.api.review

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ReviewInputTests {

    @Test
    fun `name cleaned and blank is null`() {
        assertEquals("John D.", ReviewInput.cleanName("  John D.  "))
        assertNull(ReviewInput.cleanName("   "))
        assertNull(ReviewInput.cleanName(null))
    }

    @Test
    fun `long names are trimmed`() {
        assertEquals(60, ReviewInput.cleanName("x".repeat(100))!!.length)
    }

    @Test
    fun `comment under max ok`() {
        assertEquals(1000, ReviewInput.cleanComment("a".repeat(1000))!!.length)
    }

    @Test
    fun `comment over max bad`() {
        val ex = assertThrows<ResponseStatusException> { ReviewInput.cleanComment("a".repeat(1001)) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }
}
