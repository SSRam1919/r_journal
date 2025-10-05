// app/src/main/java/com/baverika/r_journal/ui/screens/QuickNotesScreen.kt

package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.baverika.r_journal.data.local.entity.QuickNote
import com.baverika.r_journal.ui.viewmodel.QuickNoteViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(
    viewModel: QuickNoteViewModel,
    navController: NavController
) {
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())

    // State to hold the note currently being edited
    var noteToEdit by remember { mutableStateOf<QuickNote?>(null) }
    // State for the edit screen's text fields
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    // Determine if we are in edit mode
    val isEditing = noteToEdit != null

    // When entering edit mode, populate the text fields
    LaunchedEffect(noteToEdit) {
        if (isEditing) {
            editTitle = noteToEdit!!.title
            editContent = noteToEdit!!.content
        } else {
            // Clear fields when exiting edit mode
            editTitle = ""
            editContent = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Notes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("new_quick_note") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Quick Note")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // --- Main List Content ---
            if (!isEditing) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        QuickNoteItem(
                            note = note,
                            onDelete = { viewModel.deleteNote(it) },
                            // Navigate to edit mode when clicked
                            onClick = {
                                // Set the note to edit, which will trigger the LaunchedEffect above
                                noteToEdit = note
                            }
                        )
                    }
                }
            }
            // --- End Main List Content ---

            // --- Edit Note UI (Full Screen Overlay) ---
            if (isEditing) {
                // Use a Surface to create a distinct layer
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    tonalElevation = 8.dp // Give it a lifted appearance
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Bar for Edit Screen
                        TopAppBar(
                            title = { Text("Edit Quick Note") },
                            navigationIcon = {
                                IconButton(onClick = { noteToEdit = null }) { // Exit edit mode
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                // Save Button
                                IconButton(
                                    onClick = {
                                        // Validate and save
                                        val title = editTitle.trim()
                                        val content = editContent.trim()
                                        if (title.isNotBlank() || content.isNotBlank()) {
                                            // Update the existing note object
                                            val updatedNote = noteToEdit!!.copy(
                                                title = if (title.isBlank()) "Untitled" else title,
                                                content = content
                                            )
                                            viewModel.updateNote(updatedNote)
                                        }
                                        // Exit edit mode
                                        noteToEdit = null
                                    }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )

                        // Edit Fields
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = editContent,
                                onValueChange = { editContent = it },
                                label = { Text("Content") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .weight(1f),
                                maxLines = 10 // Adjust as needed
                            )
                        }
                    }
                }
            }
            // --- End Edit Note UI ---
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNoteItem(note: QuickNote, onDelete: (QuickNote) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() } // Make the whole item clickable
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDelete(note) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                }
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = formatTimestamp(note.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return LocalDateTime
        .ofInstant(java.time.Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a"))
}