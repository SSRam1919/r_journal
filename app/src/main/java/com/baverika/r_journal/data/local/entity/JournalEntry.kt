// app/src/main/java/com/baverika/r_journal/data/local/entity/JournalEntry.kt

package com.baverika.r_journal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val dateMillis: Long = 0L,
    val messages: List<ChatMessage> = emptyList(),
    val tags: List<String> = emptyList(),
    val mood: String? = null,
    val imageUris: List<String> = emptyList()
) {
    val localDate: LocalDate
        get() = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(dateMillis), ZoneId.systemDefault())

    companion object {
        fun createForToday(): JournalEntry {
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
            return JournalEntry(dateMillis = startOfDay)
        }
        fun isToday(dateMillis: Long): Boolean {
            val entryDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(dateMillis), ZoneId.systemDefault())
            return entryDate == LocalDate.now()
        }
    }



    // ✅ No-arg constructor with explicit defaults
    constructor() : this(
        id = java.util.UUID.randomUUID().toString(),
        dateMillis = 0L,
        messages = emptyList(),
        tags = emptyList(),
        mood = null
    )
}