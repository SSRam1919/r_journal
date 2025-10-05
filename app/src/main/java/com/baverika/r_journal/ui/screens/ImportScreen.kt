// app/src/main/java/com/baverika/r_journal/ui/screens/ImportScreen.kt
package com.baverika.r_journal.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.baverika.r_journal.repository.JournalRepository
import com.baverika.r_journal.repository.QuickNoteRepository
import com.baverika.r_journal.utils.ImportUtils // Import the logic object
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@Composable
fun ImportScreen(
    journalRepo: JournalRepository,
    quickNoteRepo: QuickNoteRepository
    // If you use navigation later, add navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher for picking a ZIP file
    val pickZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            // Initiate import using the ImportUtils object
            ImportUtils.importFromUri(
                context = context,
                uri = pickedUri,
                journalRepo = journalRepo,
                quickNoteRepo = quickNoteRepo,
                coroutineScope = scope // Pass the CoroutineScope
            ) { success, message ->
                // Show result in snackbar
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Import Data",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    // Launch the file picker for ZIP files
                    pickZipLauncher.launch(arrayOf("application/zip")) // MIME type for ZIP
                }
            ) {
                Text("Select ZIP File to Import")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter)
        )
    }
}