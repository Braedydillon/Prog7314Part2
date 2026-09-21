package com.example.prog7314p2.Firestore

data class CartItem(
    val productId: Int = 0,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)