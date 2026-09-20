package com.unify.api.orders

import com.unify.api.orderitem.OrderItem
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    public var id: Int? = null
    @Column(name = "user_id")
    var userId: String? = null
    @Column(name = "order_date")
    var orderDate: LocalDateTime? = null
    @Column(name = "order_status")
    var orderStatus: String? = null
    @Column(name = "address_id")
    var addressId: Int? = null
    @Column(name = "total_amount")
    var totalAmount: BigDecimal? = null
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: List<OrderItem> = emptyList()

}