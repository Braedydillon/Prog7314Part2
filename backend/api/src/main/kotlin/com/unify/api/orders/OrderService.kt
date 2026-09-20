package com.unify.api.orders

import com.unify.api.orderitem.OrderItem
import com.unify.api.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
){

    @Transactional
    fun placeOrder(userId: String, request: CreateOrderRequest): Orders{
        val order = Orders().apply {
            this.orderDate = LocalDateTime.now()
            this.orderStatus = "Pending"
            this.addressId = request.addressId
            this.userId = userId
            this.createdAt = LocalDateTime.now()
        }

        val orderItems = mutableListOf<OrderItem>()
        var total = BigDecimal.ZERO

        for (item in request.items) {
            val product = productRepository.findById(item.productId).orElseThrow { IllegalArgumentException("Product not found.") }

            val currentStock = product.stockQuantity ?: 0
            if (currentStock < item.quantity) { throw IllegalArgumentException("Insufficient stock to place order.") }

            val unitPrice = product.price!!
            val subtotal = unitPrice.multiply(BigDecimal(item.quantity))

            val orderItem = OrderItem().apply {
                this.quantity = item.quantity
                this.unitPrice = unitPrice
                this.subtotal = subtotal
                this.product = product
                this.order  = order
            }
            orderItems.add(orderItem)
            total = total.add(subtotal)

            product.stockQuantity = currentStock - item.quantity
            productRepository.save(product)
        }

        order.totalAmount = total
        order.items = orderItems
        return orderRepository.save(order)
    }

}
