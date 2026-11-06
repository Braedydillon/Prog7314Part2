
package com.example.prog7314p2.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [OfflineCartEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun offlineCartDao(): OfflineCartDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to version 2
        // Adds offline synchronization fields without deleting existing data
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE offline_cart
                    ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE offline_cart
                    ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unify_offline_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
