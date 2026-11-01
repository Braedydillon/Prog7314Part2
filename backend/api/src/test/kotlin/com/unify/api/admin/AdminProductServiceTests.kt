package com.unify.api.admin

import com.unify.api.category.Category
import com.unify.api.category.CategoryRepository
import com.unify.api.product.Product
import com.unify.api.product.ProductRepository
import com.unify.api.storage.ImageStorage
import com.unify.api.storage.ImageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional

class AdminProductServiceTests {

    private val productRepository: ProductRepository = mock()
    private val categoryRepository: CategoryRepository = mock()
    private val imageStorage: ImageStorage = mock()
    private val service = AdminProductService(productRepository, categoryRepository, imageStorage)

    private val request = CreateProductRequest(name = "Kettle", price = BigDecimal("299.99"), stock = 10, categoryId = 2)
    private val jpeg = MockMultipartFile("image", "kettle.jpg", "image/jpeg",
        byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0, 0, 0, 0))

    private fun givenCategory() {
        whenever(categoryRepository.findById(2)).thenReturn(Optional.of(Category().apply { id = 2; name = "Electronics" }))
    }

    @Test
    fun `uploads the image and saves the product with its url`() {
        givenCategory()
        whenever(imageStorage.store(eq("products"), any(), eq(ImageType.JPEG))).thenReturn("https://example/products/a.jpg")
        whenever(productRepository.save(any<Product>())).thenAnswer { it.arguments[0] as Product }

        val saved = service.createProduct(request, jpeg)

        assertEquals("https://example/products/a.jpg", saved.imageUrl)
        assertEquals(true, saved.isAvailable)
    }

    @Test
    fun `deletes the uploaded image if saving fails`() {
        givenCategory()
        whenever(imageStorage.store(any(), any(), any())).thenReturn("https://example/products/a.jpg")
        whenever(productRepository.save(any<Product>())).thenThrow(DataIntegrityViolationException("boom"))

        assertThrows<DataIntegrityViolationException> { service.createProduct(request, jpeg) }

        verify(imageStorage).delete("https://example/products/a.jpg")
    }

    @Test
    fun `unknown category is rejected before anything is uploaded`() {
        whenever(categoryRepository.findById(2)).thenReturn(Optional.empty())

        assertThrows<ResponseStatusException> { service.createProduct(request, jpeg) }

        verify(imageStorage, never()).store(any(), any(), any())
    }
}
