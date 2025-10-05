// app/src/main/java/com/baverika/r_journal/data/local/dao/JournalDao.kt

package com.baverika.r_journal.data.local.dao

import androidx.room.*
import com.baverika.r_journal.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE dateMillis = :startOfDayMillis LIMIT 1")
    suspend fun getEntryByDate(startOfDayMillis: Long): JournalEntry?

    // ✅ Ensure this function exists
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: String): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    // ✅ Ensure your search query is correct
    // Example using json_each (requires API 32+ or fallback logic)
    // If json_each doesn't work, use FTS or basic LIKE queries.
    // In JournalDao.kt - Simple LIKE search (less powerful, more compatible)
    @Query("SELECT * FROM journal_entries WHERE " +
            "messages LIKE '%' || :query || '%' " +
            "OR tags LIKE '%' || :query || '%' " +
            "ORDER BY dateMillis DESC")
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    // Alternative simple search (less powerful, but more compatible)
    // @Query("SELECT * FROM journal_entries WHERE messages LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY dateMillis DESC")
    // fun searchEntries(query: String): Flow<List<JournalEntry>>


}