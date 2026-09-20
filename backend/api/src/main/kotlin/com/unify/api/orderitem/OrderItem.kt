package com.unify.api.orderitem

import com.unify.api.orders.Orders
import com.unify.api.product.Product
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_item")
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    var id: Int? = null
    @Column(name = "quantity")
    var quantity: Int? = null
    @Column(name = "unit_price")
    var unitPrice: BigDecimal? = null
    @Column(name = "subtotal")
    var subtotal: BigDecimal? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Orders? = null


}