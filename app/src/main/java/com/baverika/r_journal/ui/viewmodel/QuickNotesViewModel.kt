// app/src/main/java/com/baverika/r_journal/ui/viewmodel/QuickNoteViewModel.kt

package com.baverika.r_journal.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baverika.r_journal.data.local.entity.QuickNote
import com.baverika.r_journal.repository.QuickNoteRepository
import kotlinx.coroutines.launch

class QuickNoteViewModel(private val repository: QuickNoteRepository) : ViewModel() {

    val allNotes = repository.allNotes

    fun addNote(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        val note = QuickNote(
            title = title.ifBlank { "Untitled" },
            content = content
        )
        viewModelScope.launch {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: QuickNote) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: QuickNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}