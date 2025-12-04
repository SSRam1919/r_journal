package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    var noteToDelete by remember { mutableStateOf<QuickNote?>(null) }
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
            if (!isEditing) {
                TopAppBar(
                    title = { Text("Quick Notes") }
                )
            }
        },
        floatingActionButton = {
            if (!isEditing) {
                FloatingActionButton(
                    onClick = { navController.navigate("new_quick_note") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Quick Note")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // --- Main List Content ---
            if (!isEditing) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    val searchQuery by viewModel.searchQuery.collectAsState()
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Search notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (notes.isEmpty()) {
                        if (searchQuery.isNotEmpty()) {
                            // Empty search results
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No notes found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Empty state (no notes at all)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "No Quick Notes Yet",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Capture quick thoughts and ideas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { navController.navigate("new_quick_note") }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Create Note")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notes) { note ->
                                QuickNoteItem(
                                    note = note,
                                    onDelete = { noteToDelete = it },
                                    onClick = {
                                        noteToEdit = note
                                    }
                                )
                            }
                        }
                    }
                }
            }
            // --- End Main List Content ---

            // --- Edit Note UI (Full Screen Overlay) ---
            if (isEditing) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Bar for Edit Screen
                        TopAppBar(
                            title = { Text("Edit Quick Note") },
                            navigationIcon = {
                                IconButton(onClick = { noteToEdit = null }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                // Save Button
                                IconButton(
                                    onClick = {
                                        val title = editTitle.trim()
                                        val content = editContent.trim()
                                        if (title.isNotBlank() || content.isNotBlank()) {
                                            val updatedNote = noteToEdit!!.copy(
                                                title = if (title.isBlank()) "Untitled" else title,
                                                content = content
                                            )
                                            viewModel.updateNote(updatedNote)
                                        }
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
                                maxLines = 10
                            )
                        }
                    }
                }
            }
            // --- End Edit Note UI ---

            if (noteToDelete != null) {
                AlertDialog(
                    onDismissRequest = { noteToDelete = null },
                    title = { Text("Delete Note") },
                    text = { Text("Are you sure you want to delete this note?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                noteToDelete?.let { viewModel.deleteNote(it) }
                                noteToDelete = null
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { noteToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
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
            .clickable { onClick() }
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDelete(note) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                }
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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