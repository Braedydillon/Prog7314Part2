package com.unify.api.category

import jakarta.persistence.*

@Entity
@Table(name = "category")

public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    var id: Int? = null
    @Column(name = "category_name")
    var name: String? = null
    @Column(name = "description")
    var description: String? = null
    @Column(name="is_active")
    var isActive: Boolean? = null

}