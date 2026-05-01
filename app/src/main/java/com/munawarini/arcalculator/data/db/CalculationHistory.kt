package com.munawarini.arcalculator.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single saved calculation.
 *
 * @param id        Auto-generated primary key.
 * @param equation  The full expression string, e.g. "12 + sin(30)".
 * @param result    The evaluated result as a formatted string, e.g. "12.5".
 * @param timestamp Unix epoch millis – used to sort history newest-first.
 * @param label     Optional user-supplied label, e.g. "Grocery Budget".
 */
@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val equation: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val label: String? = null
)
