package com.baverika.r_journal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baverika.r_journal.data.local.entity.QuickNote
import com.baverika.r_journal.repository.QuickNoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class QuickNoteViewModel(private val repository: QuickNoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allNotes = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.allNotes
        } else {
            repository.searchNotes(query)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

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