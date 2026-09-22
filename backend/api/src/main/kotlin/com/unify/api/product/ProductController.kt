package com.unify.api.product

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/products")
class ProductController(val productRepository: ProductRepository) {

    private val log = LoggerFactory.getLogger(ProductController::class.java)

    @GetMapping
    fun findAll(
        @RequestParam category: Int?,
        @RequestParam search: String?,
        @RequestParam(defaultValue = "0") page: Int
    ): ProductPageResponse{
        log.debug("Fetching products for category $category, search $search, page: $page")
        val pageable = PageRequest.of(page, 20)
        val result = productRepository.findWithFilters(category,search,pageable)

        return ProductPageResponse(
            page = result.number,
            totalPages = result.totalPages,
            products = result.content.map { it.toResponse() }
        )
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable("id") id: Int): ProductResponse {
        return productRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Product $id not found")
            }
    }
}
