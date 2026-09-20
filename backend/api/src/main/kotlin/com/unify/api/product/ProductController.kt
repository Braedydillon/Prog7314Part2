package com.unify.api.product

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
class ProductController(val productRepository: ProductRepository) {

    @GetMapping
    fun findAll(): List<ProductResponse> = productRepository.findAll().map { it.toResponse() }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable("id") id: Int): ResponseEntity<ProductResponse>? {
        return productRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())
    }
}
