
package com.example.prog7314p2.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineCartDao {

    // Insert or replace a cart item
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: OfflineCartEntity)

    // Get visible cart items, excluding locally deleted items
    @Query("""
        SELECT * FROM offline_cart
        WHERE userId = :userId AND isDeleted = 0
    """)
    suspend fun getCartItems(userId: String): List<OfflineCartEntity>

    // Count visible cart items
    @Query("""
        SELECT COUNT(*) FROM offline_cart
        WHERE userId = :userId AND isDeleted = 0
    """)
    suspend fun getCartItemCount(userId: String): Int

    // Update quantity and mark the change as pending
    @Query("""
        UPDATE offline_cart
        SET quantity = :quantity,
            updatedAt = :updatedAt,
            pendingSync = 1,
            isDeleted = 0
        WHERE userId = :userId AND productId = :productId
    """)
    suspend fun updateQuantity(
        userId: String,
        productId: Int,
        quantity: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    // Mark an item as deleted, keeping it for later synchronization
    @Query("""
        UPDATE offline_cart
        SET isDeleted = 1,
            pendingSync = 1,
            updatedAt = :updatedAt
        WHERE userId = :userId AND productId = :productId
    """)
    suspend fun markItemDeleted(
        userId: String,
        productId: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    // Get all items waiting to synchronize
    @Query("""
        SELECT * FROM offline_cart
        WHERE userId = :userId AND pendingSync = 1
        ORDER BY updatedAt ASC
    """)
    suspend fun getPendingSyncItems(
        userId: String
    ): List<OfflineCartEntity>

    // Get a specific item, including deleted items
    @Query("""
        SELECT * FROM offline_cart
        WHERE userId = :userId AND productId = :productId
        LIMIT 1
    """)
    suspend fun getCartItem(
        userId: String,
        productId: Int
    ): OfflineCartEntity?

    // Mark an item as synchronized
    @Query("""
        UPDATE offline_cart
        SET pendingSync = 0
        WHERE userId = :userId
          AND productId = :productId
          AND updatedAt = :expectedUpdatedAt
    """)
    suspend fun markAsSynced(
        userId: String,
        productId: Int,
        expectedUpdatedAt: Long
    ): Int

    // Permanently delete an item after synchronization
    @Query("""
        DELETE FROM offline_cart
        WHERE userId = :userId
          AND productId = :productId
          AND isDeleted = 1
          AND updatedAt = :expectedUpdatedAt
    """)
    suspend fun deleteSyncedItem(
        userId: String,
        productId: Int,
        expectedUpdatedAt: Long
    ): Int

    // Delete all cart records for a user
    @Query("DELETE FROM offline_cart WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    // Delete a specific item
    @Query("""
        DELETE FROM offline_cart
        WHERE userId = :userId AND productId = :productId
    """)
    suspend fun deleteCartItem(
        userId: String,
        productId: Int
    )

    // Preserve compatibility with existing ShoppingCart.kt
    @Query("DELETE FROM offline_cart WHERE userId = :userId")
    suspend fun clearCart(userId: String)

    // Remove only synchronized items during a Firestore cache refresh
    @Query("""
        DELETE FROM offline_cart
        WHERE userId = :userId AND pendingSync = 0
    """)
    suspend fun clearSyncedItems(userId: String)
}
