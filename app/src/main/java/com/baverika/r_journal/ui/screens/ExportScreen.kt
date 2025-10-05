// app/src/main/java/com/baverika/r_journal/ui/screens/ExportScreen.kt

package com.baverika.r_journal.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.baverika.r_journal.repository.JournalRepository
import com.baverika.r_journal.repository.QuickNoteRepository
import com.baverika.r_journal.utils.ExportUtils
import kotlinx.coroutines.launch

@Composable
fun ExportScreen(
    journalRepo: JournalRepository,
    quickNoteRepo: QuickNoteRepository,
    context: Context // Or get it via LocalContext if preferred
) {
    // Use collectAsState to get the current values from the Flows
    // Provide initial empty lists while the flows are collecting
    val journals by journalRepo.allEntries.collectAsState(initial = emptyList())
    val notes by quickNoteRepo.allNotes.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isExporting) {
                CircularProgressIndicator()
                Text(text = "Exporting...", modifier = Modifier.padding(top = 16.dp))
            } else {
                Button(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            // Journals and notes are now Lists, not Flows
                            // No need for .value or .first() or collecting manually here
                            val (success, message) = ExportUtils.exportAll(context, journals, notes)

                            isExporting = false
                            // Show result in snackbar
                            snackbarHostState.showSnackbar(
                                message ?: if (success) "Export successful!" else "Export failed!"
                            )
                        }
                    }
                ) {
                    Text("Export All Data (ZIP)")
                }
                // You can add more export options here later (e.g., export only journals, only notes)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter)
        )
    }
}