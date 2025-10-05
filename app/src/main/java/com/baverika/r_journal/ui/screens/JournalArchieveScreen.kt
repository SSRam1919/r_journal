// app/src/main/java/com/baverika/r_journal/ui/screens/JournalArchiveScreen.kt

package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.baverika.r_journal.data.local.entity.JournalEntry
import java.time.format.DateTimeFormatter
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalArchiveScreen(
    journalRepo: com.baverika.r_journal.repository.JournalRepository,
    onEntryClick: (JournalEntry) -> Unit // Callback to handle click, now passes the entry itself
) {
    val allEntries by journalRepo.allEntries.collectAsState(initial = emptyList())

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(allEntries) { entry ->
            JournalEntryTile(entry = entry, onClick = { onEntryClick(entry) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryTile(entry: JournalEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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

            // ✅ Removed: Display mood as text (redundant with tag)
            // entry.mood?.let { mood ->
            //     Text(
            //         text = "Mood: $mood",
            //         style = MaterialTheme.typography.bodySmall,
            //         color = MaterialTheme.colorScheme.onSurfaceVariant
            //     )
            // }

            Spacer(modifier = Modifier.height(4.dp))

            // Display a preview of the first message or a summary
            val previewText = entry.messages.firstOrNull()?.content?.take(100)
            if (previewText != null) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "No messages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display tags (if any)
            if (entry.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.tags.forEach { tag ->
                        AssistChip(
                            onClick = { /* Handle tag click */ },
                            label = { Text(text = tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}