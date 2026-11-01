package com.unify.api.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ImageValidatorTests {

    private fun bytes(vararg values: Int, padTo: Int = 32) =
        ByteArray(maxOf(padTo, values.size)).also { b -> values.forEachIndexed { i, v -> b[i] = v.toByte() } }

    @Test
    fun `recognises a JPEG`() {
        assertEquals(ImageType.JPEG, ImageValidator.validate(bytes(0xFF, 0xD8, 0xFF, 0xE0)))
    }

    @Test
    fun `recognises a PNG`() {
        assertEquals(ImageType.PNG, ImageValidator.validate(bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)))
    }

    @Test
    fun `recognises a WebP`() {
        val webp = bytes(0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50)
        assertEquals(ImageType.WEBP, ImageValidator.validate(webp))
    }

    @Test
    fun `rejects a text file pretending to be an image`() {
        val ex = assertThrows<ResponseStatusException> { ImageValidator.validate("hello, not an image".toByteArray()) }
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.statusCode)
    }

    @Test
    fun `rejects an empty file`() {
        val ex = assertThrows<ResponseStatusException> { ImageValidator.validate(ByteArray(0)) }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `rejects files over 5 MB`() {
        val tooBig = bytes(0xFF, 0xD8, 0xFF, padTo = ImageValidator.MAX_BYTES + 1)
        val ex = assertThrows<ResponseStatusException> { ImageValidator.validate(tooBig) }
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, ex.statusCode)
    }
}
