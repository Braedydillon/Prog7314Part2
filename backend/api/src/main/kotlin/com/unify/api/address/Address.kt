package com.unify.api.address

import jakarta.persistence.*


@Entity
@Table(name = "address")
class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    var id: Int? = null
    @Column(name = "user_id")
    var userId: String? = null
    @Column(name = "address_line")
    var addressLine: String? = null

    var city: String? = null
    var province: String? = null

    @Column(name = "postal_code")
    var postalCode: String? = null
}
