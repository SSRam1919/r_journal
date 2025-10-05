// app/src/main/java/com/baverika/r_journal/utils/ExportUtils.kt

package com.baverika.r_journal.utils

import android.content.Context
import android.os.Environment
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.data.local.entity.QuickNote
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Object to handle exporting journal data.
 */
object ExportUtils {

    /**
     * Exports all journal entries and quick notes to a ZIP file.
     *
     * @param context The application context.
     * @param journals The list of JournalEntry objects to export.
     * @param quickNotes The list of QuickNote objects to export.
     * @return Pair<Boolean, String?>: A pair where the first element indicates success (true) or failure (false),
     *         and the second element is the path to the exported file on success, or an error message on failure.
     */
    fun exportAll(
        context: Context,
        journals: List<JournalEntry>,
        quickNotes: List<QuickNote>
    ): Pair<Boolean, String?> {
        return try {
            // 1. Determine the export directory
            val exportDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "r_journal_exports"
            ).apply { mkdirs() }

            // 2. Create a unique filename for the export
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val zipFileName = "r_journal_export_$timestamp.zip"
            val zipFile = File(exportDir, zipFileName)

            // 3. Create the ZIP file and write entries
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                // Export Journal Entries
                journals.forEach { entry ->
                    val fileName = "journal_${entry.localDate}_${entry.id.take(8)}.md"
                    val content = buildJournalEntryMarkdown(entry)
                    zos.putNextEntry(ZipEntry(fileName))
                    zos.write(content.toByteArray())
                    zos.closeEntry()
                }

                // Export Quick Notes
                quickNotes.forEach { note ->
                    val fileName = "quicknote_${note.id.take(8)}.md"
                    val content = buildQuickNoteMarkdown(note)
                    zos.putNextEntry(ZipEntry(fileName))
                    zos.write(content.toByteArray())
                    zos.closeEntry()
                }
            }

            Pair(true, zipFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Export failed: ${e.message}")
        }
    }

    /**
     * Converts a JournalEntry into a Markdown string.
     */
    private fun buildJournalEntryMarkdown(entry: JournalEntry): String {
        return buildString {
            append("---\n")
            append("date: ${entry.localDate}\n")
            append("id: ${entry.id}\n")
            entry.mood?.let { append("mood: $it\n") }
            if (entry.tags.isNotEmpty()) {
                append("tags: [${entry.tags.joinToString(", ")}]\n")
            }
            append("---\n\n")

            entry.messages.forEach { message ->
                val time = LocalDateTime
                    .ofInstant(java.time.Instant.ofEpochMilli(message.timestamp), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                append("**[${message.role}] ($time):** ${message.content}\n\n")
            }
        }
    }

    /**
     * Converts a QuickNote into a Markdown string.
     */
    private fun buildQuickNoteMarkdown(note: QuickNote): String {
        return buildString {
            append("---\n")
            append("title: ${note.title}\n")
            append("id: ${note.id}\n")
            val createdAt = LocalDateTime
                .ofInstant(java.time.Instant.ofEpochMilli(note.timestamp), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            append("created_at: $createdAt\n")
            append("---\n\n")
            append(note.content)
        }
    }
}