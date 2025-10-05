// app/src/main/java/com/baverika/r_journal/ui/screens/DashboardScreen.kt

package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baverika.r_journal.repository.JournalRepository
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(journalRepo: JournalRepository) {
    var totalEntries by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        journalRepo.allEntries.collectLatest { entries ->
            totalEntries = entries.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Journal Entries", style = MaterialTheme.typography.titleMedium)
                Text("$totalEntries", style = MaterialTheme.typography.headlineSmall)
            }
        }

        // Add mood chart, streaks, etc. later
    }
}