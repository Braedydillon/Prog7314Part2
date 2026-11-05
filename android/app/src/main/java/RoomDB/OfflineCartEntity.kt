
package com.example.prog7314p2.RoomDB

import androidx.room.Entity

@Entity(
    tableName = "offline_cart",
    primaryKeys = ["userId", "productId"]
)
data class OfflineCartEntity(
    val userId: String,
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?,
    val updatedAt: Long = System.currentTimeMillis(),

    // True when a local change still needs to sync to Firestore
    val pendingSync: Boolean = false,

    // True when an item was removed offline
    val isDeleted: Boolean = false
)
