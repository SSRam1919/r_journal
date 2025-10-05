// app/src/main/java/com/baverika/r_journal/utils/LegacyImportUtils.kt

package com.baverika.r_journal.utils

import android.content.Context
import android.net.Uri
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.data.local.entity.ChatMessage
import com.baverika.r_journal.data.local.entity.QuickNote
import com.baverika.r_journal.repository.JournalRepository
import com.baverika.r_journal.repository.QuickNoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

/**
 * Object to handle importing journal data.
 */
object ImportUtils {

    /**
     * Imports data from a ZIP file Uri.
     *
     * @param context The application context. Needed to open the Uri.
     * @param uri The Uri of the ZIP file to import.
     * @param journalRepo The JournalRepository instance.
     * @param quickNoteRepo The QuickNoteRepository instance.
     * @param coroutineScope The CoroutineScope to launch the import operation on.
     * @param onResult A callback function `(Boolean, String) -> Unit` that reports the result.
     *                 The first parameter is success (true/false), the second is a message.
     */
    fun importFromUri(
        context: Context,
        uri: Uri,
        journalRepo: JournalRepository,
        quickNoteRepo: QuickNoteRepository,
        coroutineScope: CoroutineScope,
        onResult: (Boolean, String) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // 1. Get InputStream from the Uri (requires content resolver)
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open input stream for URI: $uri")

                // 2. Delegate the actual import logic to a function that works with InputStream
                importFromInputStream(inputStream, journalRepo, quickNoteRepo, onResult)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Failed to open file: ${e.message}")
            }
        }
    }

    /**
     * Core import logic that works with an InputStream.
     * This makes it easier to test and reuse.
     *
     * @param inputStream The InputStream of the ZIP file to import.
     * @param journalRepo The JournalRepository instance.
     * @param quickNoteRepo The QuickNoteRepository instance.
     * @param onResult Callback for results.
     */
    private suspend fun importFromInputStream( // <-- Make this function 'suspend'
        inputStream: InputStream,
        journalRepo: JournalRepository,
        quickNoteRepo: QuickNoteRepository,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
            var journalCount = 0
            var quickNoteCount = 0

            ZipInputStream(inputStream).use { zis ->
                var zipEntry = zis.nextEntry

                while (zipEntry != null) {
                    // Process only .md files inside the zip
                    if (!zipEntry.isDirectory && zipEntry.name.endsWith(".md")) {
                        val content = zis.bufferedReader().readText()

                        if (zipEntry.name.startsWith("journal_")) {
                            val entry = parseJournalEntryMarkdown(content)
                            entry?.let {
                                // Use journalRepo to save/upsert the entry
                                // This call is now valid because this function is 'suspend'
                                journalRepo.upsertEntry(it)
                                journalCount++
                            }
                        } else if (zipEntry.name.startsWith("quicknote_")) {
                            val note = parseQuickNoteMarkdown(content)
                            note?.let {
                                // Use quickNoteRepo to save/upsert the note
                                // This call is now valid because this function is 'suspend'
                                quickNoteRepo.upsertNote(it)
                                quickNoteCount++
                            }
                        }
                        // else: ignore unknown file types
                    }
                    zipEntry = zis.nextEntry
                }
            }
            onResult(true, "Successfully imported $journalCount journal entries and $quickNoteCount quick notes.")
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "Import failed during processing: ${e.message}")
        }
        // finally block for inputStream closure is handled by ZipInputStream.use {}
    }


    /**
     * Parses a Markdown string into a JournalEntry.
     * (This is a simplified parser. A real one would be more robust).
     */
    private fun parseJournalEntryMarkdown(content: String): JournalEntry? {
        return try {
            val lines = content.lines()
            var inFrontMatter = false
            val frontMatterLines = mutableListOf<String>()
            val contentLines = mutableListOf<String>()

            for (line in lines) {
                if (line.trim() == "---") {
                    if (!inFrontMatter) {
                        inFrontMatter = true
                        continue // Skip the opening ---
                    } else {
                        inFrontMatter = false
                        continue // Skip the closing ---
                    }
                }
                if (inFrontMatter) {
                    frontMatterLines.add(line)
                } else {
                    contentLines.add(line)
                }
            }

            // Parse Front Matter
            val metaData = mutableMapOf<String, String?>()
            for (fmLine in frontMatterLines) {
                val parts = fmLine.split(":", limit = 2)
                if (parts.size == 2) {
                    metaData[parts[0].trim()] = parts[1].trim()
                }
            }

            val id = metaData["id"] ?: return null // ID is required
            val dateString = metaData["date"] ?: return null // Date is required
            val localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            val startOfDayMillis = localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000

            val mood = metaData["mood"]
            val tagsString = metaData["tags"]
            val tags = if (tagsString != null) {
                tagsString.removePrefix("[").removeSuffix("]").split(",").map { it.trim().removeSurrounding("\"") }
            } else {
                emptyList()
            }

            // Parse Messages (simplified)
            val messages = contentLines.filter { it.isNotBlank() }.mapNotNull { line ->
                // This is a very basic parser. Real implementation needed.
                // Example line: "**[user] (10:30:15):** This is a message"
                // Improved regex to be more specific about the time format
                val regex = Regex("""\*\*\[([^\]]+)\] \((\d{2}:\d{2}:\d{2})\):\*\* (.+)""")
                val matchResult = regex.find(line)
                if (matchResult != null) {
                    val role = matchResult.groupValues[1]
                    val timeString = matchResult.groupValues[2]
                    val content = matchResult.groupValues[3]
                    // Reconstruct timestamp (approximate based on entry date)
                    val time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val timestamp = localDate.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ChatMessage(role = role, content = content, timestamp = timestamp)
                } else {
                    // Optionally log lines that couldn't be parsed if needed for debugging
                    // println("Could not parse message line: $line")
                    null
                }
            }

            JournalEntry(
                id = id,
                dateMillis = startOfDayMillis,
                messages = messages,
                tags = tags,
                mood = mood
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Consider logging the specific content that failed to parse
            // Log.e("ImportUtils", "Failed to parse JournalEntry Markdown: $content", e)
            null // Parsing failed
        }
    }

    /**
     * Parses a Markdown string into a QuickNote.
     * (This is a simplified parser. A real one would be more robust).
     */
    private fun parseQuickNoteMarkdown(content: String): QuickNote? {
        return try {
            val lines = content.lines()
            var inFrontMatter = false
            val frontMatterLines = mutableListOf<String>()
            val contentLines = mutableListOf<String>()

            for (line in lines) {
                if (line.trim() == "---") {
                    if (!inFrontMatter) {
                        inFrontMatter = true
                        continue
                    } else {
                        inFrontMatter = false
                        continue
                    }
                }
                if (inFrontMatter) {
                    frontMatterLines.add(line)
                } else {
                    contentLines.add(line)
                }
            }

            // Parse Front Matter
            val metaData = mutableMapOf<String, String?>()
            for (fmLine in frontMatterLines) {
                val parts = fmLine.split(":", limit = 2)
                if (parts.size == 2) {
                    metaData[parts[0].trim()] = parts[1].trim()
                }
            }

            val id = metaData["id"] ?: return null // ID is required
            val title = metaData["title"] ?: "Untitled"
            val createdAtString = metaData["created_at"]
            val timestamp = if (createdAtString != null) {
                LocalDateTime.parse(createdAtString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                System.currentTimeMillis() // Fallback
            }

            val noteContent = contentLines.joinToString("\n")

            QuickNote(
                id = id,
                title = title,
                content = noteContent,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Consider logging the specific content that failed to parse
            // Log.e("ImportUtils", "Failed to parse QuickNote Markdown", e)
            null // Parsing failed
        }
    }
}