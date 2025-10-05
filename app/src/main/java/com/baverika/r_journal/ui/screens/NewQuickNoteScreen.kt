// app/src/main/java/com/baverika/r_journal/ui/screens/NewQuickNoteScreen.kt

package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // Add this import
import com.baverika.r_journal.ui.viewmodel.QuickNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewQuickNoteScreen(
    viewModel: QuickNoteViewModel,
    navController: NavController // Receive NavController
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // Handle back button press (optional: prompt if content is unsaved)
    // You might want to use DisposableEffect or rememberSaveable for this

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Quick Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Navigate back using NavController
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                viewModel.addNote(title, content)
                            }
                            navController.popBackStack() // Navigate back after saving using NavController
                        },
                        enabled = title.isNotBlank() || content.isNotBlank() // Enable only if there's content
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight() // Take remaining vertical space
                        .weight(1f), // Use weight if inside another Column
                    maxLines = 10 // Adjust as needed
                )
            }
        }
    )
}