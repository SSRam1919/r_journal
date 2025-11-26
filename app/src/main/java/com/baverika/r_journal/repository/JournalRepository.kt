package com.baverika.r_journal.repository

import android.util.Log
import com.baverika.r_journal.data.local.dao.JournalDao
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.data.remote.RetrofitClient
import com.baverika.r_journal.data.remote.toDto
import com.baverika.r_journal.data.remote.toEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class JournalRepository(
    private val journalDao: JournalDao
) {
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    private fun todayStartMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000

    suspend fun getOrCreateTodaysEntry(): JournalEntry {
        val todayStart = todayStartMillis()
        return journalDao.getEntryByDate(todayStart) ?: JournalEntry.createForToday()
    }

    suspend fun saveEntry(entry: JournalEntry) {
        // always keep messages ordered
        val sorted = entry.copy(
            messages = entry.messages.sortedBy { it.timestamp }
        )
        journalDao.insertEntry(sorted)
    }

    suspend fun getEntryById(id: String): JournalEntry? {
        return journalDao.getEntryById(id)
    }

    fun search(query: String): Flow<List<JournalEntry>> = journalDao.searchEntries(query)

    suspend fun upsertEntry(entry: JournalEntry) {
        saveEntry(entry)
    }

    // ---------- NEW: sync with Flask API ----------

    suspend fun syncTodayFromServer(local: JournalEntry?): JournalEntry? {
        return try {
            val dto = RetrofitClient.getApi().getToday()
            val remote = dto.toEntity()

            // Base = what we already have locally
            val base = local ?: remote

            // Merge messages: local(app) + remote(site)
            val mergedMessages = (local?.messages ?: emptyList()) + remote.messages

            val distinctMessages = mergedMessages
                .distinctBy { Pair(it.timestamp, it.content) }
                .sortedBy { it.timestamp }

            // ✅ Only update messages (and optionally id).
            //    Mood, tags, imageUris stay exactly as in 'base' (app).
            val mergedEntry = base.copy(
                id = remote.id,          // or keep base.id if you prefer
                messages = distinctMessages
                // tags = base.tags
                // mood = base.mood
                // imageUris = base.imageUris
            )

            saveEntry(mergedEntry)
            mergedEntry
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



//    suspend fun pushTodayToServer(entry: JournalEntry) {
//        try {
//            val dto = entry.toDto()
//            Log.d("JournalRepo", "Pushing to server: ${dto.messages.size} messages")
//            val savedDto = RetrofitClient.api.saveToday(dto)
//            val savedEntity = savedDto.toEntity()
//            saveEntry(savedEntity) // keep DB in sync with whatever server has
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("JournalRepo", "pushTodayToServer failed", e)
//            // You can decide to ignore or bubble up error
//        }
//    }
}
