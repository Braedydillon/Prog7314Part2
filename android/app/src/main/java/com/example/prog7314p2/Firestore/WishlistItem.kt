package com.example.prog7314p2.Firestore

data class WishlistItem(
    val productId: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)