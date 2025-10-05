// app/src/main/java/com/baverika/r_journal/ui/viewmodel/JournalViewModel.kt

package com.baverika.r_journal.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baverika.r_journal.data.local.entity.ChatMessage
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.repository.JournalRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import android.content.Context
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




class JournalViewModel(private val repo: JournalRepository, private val context: Context) : ViewModel() {
    // State for the currently loaded/active entry


    var currentEntry by mutableStateOf(JournalEntry.createForToday())
        private set

    // State to track if the current entry is for today (or a past entry)
    val isCurrentEntryToday: Boolean
        get() = JournalEntry.isToday(currentEntry.dateMillis) // ✅ Call the companion object function

    init {
        // Optionally load today's entry on init, or rely on explicit loading
        loadTodaysEntry()
    }

    fun loadTodaysEntry() {
        viewModelScope.launch {
            currentEntry = repo.getOrCreateTodaysEntry()
        }
    }

    // Function to load a specific entry for editing/appending
    fun loadEntryForEditing(entryId: String) {
        viewModelScope.launch {
            // ✅ Call the repository function
            val entry = repo.getEntryById(entryId)
            if (entry != null) {
                currentEntry = entry
                // No need to save here, as we are just loading for viewing/editing
            } else {
                // Handle case where entry ID is invalid or not found
                // For now, maybe just log or load today's entry as fallback
                println("Warning: Entry with ID $entryId not found. Loading today's entry.")
                loadTodaysEntry() // Or handle differently
            }
        }
    }

    // Modified addMessage to work with the currently loaded entry
    fun addMessage(content: String) {
        if (content.isBlank()) return
        val msg = ChatMessage(
            role = "user",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        // Append the new message to the currentEntry's message list
        currentEntry = currentEntry.copy(messages = currentEntry.messages + msg)
        saveCurrentEntry() // Save the updated entry
    }

    // --- Add this new function ---
    fun addMessageWithImage(content: String, imageUri: String?) {
        if (content.isBlank() && imageUri == null) return

        var savedImagePath: String? = null
        imageUri?.let { uriString ->
            try {
                // Convert the string URI to a Uri object
                val uri: Uri = Uri.parse(uriString)

                // 1. Save the image to app-private storage
                savedImagePath = saveImageToPrivateStorage(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                // --- ✅ ADD TOAST ERROR HANDLING HERE ---
                Toast.makeText(context, "Failed to save image. Please try again.", Toast.LENGTH_SHORT).show()
                // --- ✅ END TOAST ERROR HANDLING ---
            }
        }

        val msg = ChatMessage(
            role = "user",
            content = content,
            timestamp = System.currentTimeMillis(),
            imageUri = savedImagePath // Pass the image URI
        )

        currentEntry = currentEntry.copy(messages = currentEntry.messages + msg)
        saveCurrentEntry()
    }

    /**
     * Saves an image from a URI to the app's private storage.
     *
     * @param imageUri The URI of the image to save.
     * @return The absolute path to the saved image file.
     */
    private fun saveImageToPrivateStorage(imageUri: Uri): String {
        // 1. Generate a unique filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"

        // 2. Get the app's private pictures directory
        val storageDir: File? = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)

        // 3. Create the output file
        val imageFile = File(storageDir, fileName)

        // 4. Copy the image data from the URI to the file
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            FileOutputStream(imageFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // 5. Return the absolute path
        return imageFile.absolutePath
    }

    fun editMessage(messageId: String, newContent: String) {
        if (!isCurrentEntryToday) return // ❌ Block editing for past entries
        if (newContent.isBlank()) {
            deleteMessage(messageId) // Treat blank edit as delete
            return
        }
        val updatedMessages = currentEntry.messages.map { msg ->
            if (msg.id == messageId) {
                msg.copy(content = newContent, timestamp = System.currentTimeMillis())
            } else {
                msg
            }
        }
        currentEntry = currentEntry.copy(messages = updatedMessages)
        saveCurrentEntry()
    }

    /**
     * Deletes a message from the current journal entry.
     * This function only affects messages in today's entry.
     *
     * @param messageId The ID of the message to delete.
     */
    fun deleteMessage(messageId: String) {
        if (!isCurrentEntryToday) return // ❌ Block deleting for past entries
        val updatedMessages = currentEntry.messages.filterNot { it.id == messageId }

        val deletedMessage = currentEntry.messages.find { it.id == messageId }
        deletedMessage?.imageUri?.let { imagePath ->
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                imageFile.delete()
            }
        }


        currentEntry = currentEntry.copy(messages = updatedMessages)
        saveCurrentEntry()
    }

    fun updateMood(mood: String) {
        val moodTag = "#mood-$mood"
        val updatedTags = currentEntry.tags
            .filterNot { it.startsWith("#mood-") }
            .plus(moodTag)
        currentEntry = currentEntry.copy(mood = mood, tags = updatedTags)
        saveCurrentEntry()
    }

    private fun saveCurrentEntry() {
        viewModelScope.launch {
            repo.saveEntry(currentEntry)
        }
    }
}