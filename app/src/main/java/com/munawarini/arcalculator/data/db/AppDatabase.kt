package com.munawarini.arcalculator.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Singleton Room database for AR-Calculator.
 * Version history:
 *   v1 – Initial schema with [CalculationHistory].
 */
@Database(
    entities = [CalculationHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calculationHistoryDao(): CalculationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance, creating it lazily if needed.
         * Uses double-checked locking for thread safety.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ar_calculator.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
