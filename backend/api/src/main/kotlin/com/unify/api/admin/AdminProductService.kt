package com.unify.api.admin

import com.unify.api.category.CategoryRepository
import com.unify.api.product.Product
import com.unify.api.product.ProductRepository
import com.unify.api.storage.ImageStorage
import com.unify.api.storage.ImageValidator
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val imageStorage: ImageStorage
) {

    fun createProduct(request: CreateProductRequest, image: MultipartFile?): Product {
        val valid = ProductValidator.validate(request)
        val category = categoryRepository.findById(valid.categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ${valid.categoryId} not found") }

        val imageUrl = image?.takeUnless { it.isEmpty }?.let { file ->
            val bytes = file.bytes
            val type = ImageValidator.validate(bytes)
            imageStorage.store("products", bytes, type)
        }

        val product = Product().apply {
            productName = valid.name
            description = valid.description
            price = valid.price
            discountPrice = valid.discountPrice
            stockQuantity = valid.stock
            brand = valid.brand
            this.imageUrl = imageUrl
            isAvailable = true
            createdAt = LocalDateTime.now()
            this.category = category
        }

        return try {
            productRepository.save(product)
        } catch (e: Exception) {
            imageUrl?.let { imageStorage.delete(it) }
            throw e
        }
    }
}
