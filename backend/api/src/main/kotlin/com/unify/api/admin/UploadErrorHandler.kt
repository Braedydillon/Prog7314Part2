package com.unify.api.admin

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class UploadErrorHandler {

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun tooLarge(e: MaxUploadSizeExceededException): ResponseEntity<ProblemDetail> =
        ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
            .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "Image must be 5 MB or smaller"))
}
