package com.unify.api.admin

import com.unify.api.product.ProductResponse
import com.unify.api.product.toResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/products")
class AdminProductController(private val adminProductService: AdminProductService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProduct(
        @RequestPart("product") request: CreateProductRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ProductResponse> {
        val saved = adminProductService.createProduct(request, image)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }
}
