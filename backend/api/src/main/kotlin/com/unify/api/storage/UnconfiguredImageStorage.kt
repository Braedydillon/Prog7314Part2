package com.unify.api.storage

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UnconfiguredImageStorage : ImageStorage {

    override fun store(folder: String, bytes: ByteArray, type: ImageType): String =
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image storage isn't configured")

    override fun delete(url: String) = Unit
}
