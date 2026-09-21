package com.unify.api.product

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/products")
class ProductController(val productRepository: ProductRepository) {

    @GetMapping
    fun findAll(
        @RequestParam category: Int?,
        @RequestParam search: String?,
        @RequestParam(defaultValue = "0") page: Int
    ): ProductPageResponse{
        val pageable = PageRequest.of(page, 20)
        val result = productRepository.findWithFilters(category,search,pageable)

        return ProductPageResponse(
            page = result.number,
            totalPages = result.totalPages,
            products = result.content.map { it.toResponse() }
        )
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable("id") id: Int): ResponseEntity<ProductResponse>? {
        return productRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())
    }
}
