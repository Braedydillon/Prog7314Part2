package com.example.prog7314p2.Models

import java.io.Serializable

data class Category(
    val id: Int,
    val name: String,
    val description: String?,
    val active: Boolean
) : Serializable