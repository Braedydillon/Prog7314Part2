package com.unify.api.storage

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

enum class ImageType(val contentType: String, val extension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp")
}

object ImageValidator {

    const val MAX_BYTES = 5 * 1024 * 1024

    fun validate(bytes: ByteArray): ImageType {
        if (bytes.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is empty")
        }
        if (bytes.size > MAX_BYTES) {
            throw ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Image must be 5 MB or smaller")
        }
        return detectType(bytes)
            ?: throw ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Only JPEG, PNG or WebP images are allowed"
            )
    }

    fun detectType(bytes: ByteArray): ImageType? = when {
        bytes.matches(0, 0xFF, 0xD8, 0xFF) -> ImageType.JPEG
        bytes.matches(0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> ImageType.PNG
        bytes.matches(0, 0x52, 0x49, 0x46, 0x46) && bytes.matches(8, 0x57, 0x45, 0x42, 0x50) -> ImageType.WEBP
        else -> null
    }

    private fun ByteArray.matches(offset: Int, vararg expected: Int): Boolean =
        size >= offset + expected.size &&
            expected.indices.all { this[offset + it] == expected[it].toByte() }
}
