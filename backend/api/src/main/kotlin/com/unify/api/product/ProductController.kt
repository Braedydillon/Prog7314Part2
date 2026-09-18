package com.unify.api.product

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(val productRepository: ProductRepository) {

    @GetMapping
    fun findAll(): List<Product> = productRepository.findAll()

    @GetMapping("/{id}")
    fun getProductById(@PathVariable("id") id: Int): ResponseEntity<Product> {
        return productRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }
}