package com.unify.api.profile

import com.unify.api.storage.ImageStorage
import com.unify.api.storage.ImageValidator
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarService(private val imageStorage: ImageStorage) {

    fun uploadAvatar(userId: String, image: MultipartFile): String {
        val bytes = image.bytes
        val type = ImageValidator.validate(bytes)

        val folder = "avatars/$userId"
        val url = imageStorage.store(folder, bytes, type)
        imageStorage.deleteFolderExcept(folder, url)
        return url
    }
}
