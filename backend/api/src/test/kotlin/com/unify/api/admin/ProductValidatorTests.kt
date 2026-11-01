package com.unify.api.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

class ProductValidatorTests {

    private val valid = CreateProductRequest(
        name = "  Kettle  ", description = "  ", price = BigDecimal("299.99"),
        discountPrice = BigDecimal("249.99"), stock = 10, brand = "Defy", categoryId = 2
    )

    private fun errorFor(request: CreateProductRequest): String =
        assertThrows<ResponseStatusException> { ProductValidator.validate(request) }.reason ?: ""

    @Test
    fun `valid request passes and is cleaned up`() {
        val result = ProductValidator.validate(valid)
        assertEquals("Kettle", result.name)
        assertNull(result.description)
        assertEquals(10, result.stock)
    }

    @Test
    fun `name is required and limited to 200 characters`() {
        assertTrue(errorFor(valid.copy(name = " ")).contains("Name is required"))
        assertTrue(errorFor(valid.copy(name = "x".repeat(201))).contains("200 characters"))
    }

    @Test
    fun `price must be positive with at most 2 decimals`() {
        assertTrue(errorFor(valid.copy(price = BigDecimal.ZERO, discountPrice = null)).contains("Price must be greater than 0"))
        assertTrue(errorFor(valid.copy(price = BigDecimal("10.505"), discountPrice = null)).contains("2 decimal places"))
    }

    @Test
    fun `sale price must be lower than the price`() {
        assertTrue(errorFor(valid.copy(discountPrice = BigDecimal("299.99"))).contains("lower than the price"))
    }

    @Test
    fun `stock can't be negative and brand is limited`() {
        assertTrue(errorFor(valid.copy(stock = -1)).contains("Stock can't be negative"))
        assertTrue(errorFor(valid.copy(brand = "b".repeat(101))).contains("100 characters"))
    }

    @Test
    fun `all problems are reported together`() {
        val message = errorFor(CreateProductRequest())
        assertTrue(message.contains("Name is required"))
        assertTrue(message.contains("Price is required"))
        assertTrue(message.contains("Stock is required"))
        assertTrue(message.contains("Category is required"))
    }
}
