package com.unify.api.orders

import com.unify.api.address.AddressRepository
import com.unify.api.orderitem.OrderItem
import org.springframework.http.HttpStatus
import com.unify.api.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import org.springframework.web.server.ResponseStatusException

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val addressRepository: AddressRepository
){

    @Transactional
    fun placeOrder(userId: String, request: CreateOrderRequest): Orders{

        if (request.items.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item")
        }

        val address = addressRepository.findById(request.addressId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Address ${request.addressId} not found") }

        if (address.userId != userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Address ${request.addressId} not found")
        }

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

            if (item.quantity <=0){
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Order quantity must be greater than zero")
            }

            val product = productRepository.findById(item.productId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product ${item.productId} not found") }

            val currentStock = product.stockQuantity ?: 0

            if (currentStock < item.quantity) { throw ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock. Requested: ${item.quantity}, Available: $currentStock")
            }

            val unitPrice = product.price
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Product ${item.productId} has no price set")
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
