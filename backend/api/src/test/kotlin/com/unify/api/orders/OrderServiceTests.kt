package com.unify.api.orders

import com.unify.api.address.Address
import com.unify.api.address.AddressRepository
import com.unify.api.product.Product
import com.unify.api.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Optional

class OrderServiceTests {
    private val orderRepository: OrderRepository = mock()
    private val productRepository: ProductRepository = mock()
    private val addressRepository: AddressRepository = mock()
    private val service = OrderService(orderRepository, productRepository, addressRepository)

    private fun stubValidAddress(userId: String) {
        val address = Address().apply { id = 1; this.userId = userId }
        whenever(addressRepository.findById(1)).thenReturn(Optional.of(address))
    }

    @Test
    fun `fails when not enough stock`() {
        stubValidAddress("test_user")
        val product = Product().apply { id = 1; stockQuantity = 2; price = BigDecimal("666.00") }
        whenever(productRepository.findById(1)).thenReturn(Optional.of(product))

        val request = CreateOrderRequest(items = listOf(OrderItemRequest(productId = 1, quantity = 5)), addressId = 1)

        assertThrows<ResponseStatusException> {
            service.placeOrder("test_user", request)
        }
    }

    @Test
    fun `fails when product not found`() {
        stubValidAddress("test_user")
        whenever(productRepository.findById(1231274)).thenReturn(Optional.empty())
        val request = CreateOrderRequest(items = listOf(OrderItemRequest(productId = 1231274, quantity = 1)), addressId = 1)

        assertThrows<ResponseStatusException> {
            service.placeOrder("test_user", request)
        }
    }

    @Test
    fun `remove stock and snap price on good order`() {
        stubValidAddress("test-user")
        val product = Product().apply { id = 1; stockQuantity = 10; price = BigDecimal("67.00") }
        whenever(productRepository.findById(1)).thenReturn(Optional.of(product))
        whenever(productRepository.save(any())).thenReturn(product)
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Orders }

        val request = CreateOrderRequest(items = listOf(OrderItemRequest(productId = 1, quantity = 3)), addressId = 1)
        val order = service.placeOrder("test-user", request)

        assertEquals(BigDecimal("201.00"), order.totalAmount)
        assertEquals(7, product.stockQuantity)
        verify(productRepository).save(product)
    }
}