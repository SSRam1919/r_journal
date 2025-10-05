// app/src/main/java/com/baverika/r_journal/repository/JournalRepository.kt

package com.baverika.r_journal.repository

import com.baverika.r_journal.data.local.dao.JournalDao
import com.baverika.r_journal.data.local.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {
    val allEntries = journalDao.getAllEntries()

    suspend fun getOrCreateTodaysEntry(): JournalEntry {
        val todayStart = JournalEntry.createForToday().dateMillis
        return journalDao.getEntryByDate(todayStart) ?: JournalEntry.createForToday()
        TODO("Implement getOrCreateTodaysEntry")
    }

    suspend fun saveEntry(entry: JournalEntry) {
        journalDao.insertEntry(entry)
    }

    // ✅ Ensure this function exists
    suspend fun getEntryById(id: String): JournalEntry? {
        return journalDao.getEntryById(id)
    }

    // ✅ ADD THIS: Search method
    fun search(query: String): Flow<List<JournalEntry>> = journalDao.searchEntries(query)

    suspend fun upsertEntry(entry: JournalEntry) {
        // This relies on your JournalDao's insertEntry method using
        // @Insert(onConflict = OnConflictStrategy.REPLACE).
        // REPLACE means if an entry with the same @PrimaryKey (id) exists, it will be replaced.
        //journalDao.insertEntry(entry)
        saveEntry(entry)
    }

}