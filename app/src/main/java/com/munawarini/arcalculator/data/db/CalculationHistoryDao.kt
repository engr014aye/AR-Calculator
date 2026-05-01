package com.munawarini.arcalculator.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [CalculationHistory].
 * All reactive queries return [Flow] so the UI layer observes changes automatically.
 */
@Dao
interface CalculationHistoryDao {

    /** Insert a new history record. Replaces on conflict (e.g. same PK). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CalculationHistory)

    /** Delete a single record by its ID. */
    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Delete all records from the table. */
    @Query("DELETE FROM calculation_history")
    suspend fun deleteAll()

    /**
     * Observe all history entries ordered by most-recent first.
     * Returns a cold [Flow] that re-emits on every DB change.
     */
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    /** Update the user-supplied label for a history entry. */
    @Query("UPDATE calculation_history SET label = :label WHERE id = :id")
    suspend fun updateLabel(id: Long, label: String?)
}
