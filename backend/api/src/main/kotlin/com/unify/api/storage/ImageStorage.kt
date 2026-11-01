package com.unify.api.storage

interface ImageStorage {

    fun store(folder: String, bytes: ByteArray, type: ImageType): String

    fun delete(url: String)
}
