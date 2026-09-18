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

fun Product.toResponse(): ProductResponse = ProductResponse(
    id = this.id!!,
    name = this.productName ?: "",
    description = this.description ?: "",
    price = this.price ?: BigDecimal.ZERO,
    discount = this.discountPrice,
    stock = this.stockQuantity ?: 0,
    imageUrl = this.imageUrl ?: "",
    brand = this.brand,
    available = this.isAvailable ?: false,
    category = CategorySummary(
        id = this.category?.id ?: 0,
        name = this.category?.name ?: ""
    )
)