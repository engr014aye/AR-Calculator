package com.munawarini.arcalculator.data.repository

import com.munawarini.arcalculator.data.db.CalculationHistory
import com.munawarini.arcalculator.data.db.CalculationHistoryDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository that abstracts the Room data source from the ViewModel.
 * The ViewModel only depends on this interface, keeping the data layer swappable.
 */
class HistoryRepository(private val dao: CalculationHistoryDao) {

    /** Cold flow of all history entries, newest first. */
    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

    /** Persist a new calculation entry. */
    suspend fun insert(history: CalculationHistory) = dao.insert(history)

    /** Remove a specific entry by its database ID. */
    suspend fun deleteById(id: Long) = dao.deleteById(id)

    /** Wipe the entire history ledger. */
    suspend fun deleteAll() = dao.deleteAll()

    /** Update the optional user label on a history entry. */
    suspend fun updateLabel(id: Long, label: String?) = dao.updateLabel(id, label)
}
