// app/src/main/java/com/baverika/r_journal/ui/screens/SearchScreen.kt

package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baverika.r_journal.data.local.entity.JournalEntry
import com.baverika.r_journal.ui.viewmodel.SearchViewModel
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController // Import NavController for navigation

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavController // Receive NavController for navigation
) {
    // Collect the search query and results from the ViewModel
    val query by viewModel.query.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateQuery(it) }, // Update query in ViewModel
            label = { Text("Search journals") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            // Display each search result entry
            items(results) { entry ->
                // Pass the query to the tile so it can highlight or preview the matching part
                JournalEntryTile(
                    entry = entry,
                    onClick = {
                        // Navigate to ChatInputScreen for the specific entry
                        navController.navigate("chat_input/${entry.id}")
                    },
                    query = query
                )
            }
        }
    }
}

// Updated JournalEntryTile to accept and use the search query for preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEntryTile(entry: JournalEntry, onClick: () -> Unit, query: String? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, // Make the whole item clickable and call onClick
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Display date
            Text(
                text = entry.localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Display tags (if any)
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.tags.forEach { tag ->
                        AssistChip(
                            onClick = { /* Handle tag click if needed */ },
                            label = { Text(text = tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display a preview of the message that matches the query, or the first message if no query or no match
            // This makes the search results more relevant
            val previewText = if (!query.isNullOrEmpty()) {
                // Find the first message containing the query (case-insensitive)
                entry.messages.firstOrNull { msg -> msg.content.contains(query, ignoreCase = true) }?.content
            } else {
                // If no query, show the first message
                entry.messages.firstOrNull()?.content
            }

            if (previewText != null) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4, // Limit lines to keep tiles manageable
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "No messages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}