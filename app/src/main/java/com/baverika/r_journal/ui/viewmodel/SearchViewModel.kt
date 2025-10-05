// app/src/main/java/com/baverika/r_journal/ui/viewmodel/SearchViewModel.kt

package com.baverika.r_journal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context

class SearchViewModel(
    private val journalRepo: JournalRepository,
    private val context: Context
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults: StateFlow<List<JournalEntry>> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { queryString ->
            if (queryString.isBlank()) {
                flowOf(emptyList())
            } else {
                journalRepo.search(queryString)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }
}