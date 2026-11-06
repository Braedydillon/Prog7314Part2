
package com.example.prog7314p2

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.example.prog7314p2.RoomDB.AppDatabase
import com.example.prog7314p2.RoomDB.OfflineCartEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class OfflineCartDatabaseTest {

    @Test
    fun insertAndReadCartItem() = runBlocking {

        val context = ApplicationProvider
            .getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        try {
            val cartDao = database.offlineCartDao()

            val item = OfflineCartEntity(
                userId = "testUser",
                productId = 1,
                name = "Test Product",
                price = 100.0,
                quantity = 2,
                imageUrl = null
            )

            cartDao.insertCartItem(item)

            val savedItems = cartDao.getCartItems("testUser")

            assertEquals(1, savedItems.size)
            assertEquals("Test Product", savedItems[0].name)
            assertEquals(2, savedItems[0].quantity)

        } finally {
            database.close()
        }
    }
}
