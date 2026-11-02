package com.unify.api.profile

import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

data class AvatarResponse(val url: String)

@RestController
@RequestMapping("/api/me/avatar")
class AvatarController(private val avatarService: AvatarService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        authentication: Authentication,
        @RequestPart("image") image: MultipartFile
    ): AvatarResponse = AvatarResponse(avatarService.uploadAvatar(authentication.name, image))
}
