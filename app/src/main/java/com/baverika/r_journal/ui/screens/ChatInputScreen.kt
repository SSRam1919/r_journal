// app/src/main/java/com/baverika/r_journal/ui/screens/ChatInputScreen.kt

package com.baverika.r_journal.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.baverika.r_journal.data.local.entity.ChatMessage
import com.baverika.r_journal.ui.viewmodel.JournalViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController // ✅ Add this import
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // ✅ Add this import
import com.baverika.r_journal.R // ✅ Import your app's R class

// --- Composable Functions: MoodPicker, MoodButton, ChatBubble ---

@Composable
fun MoodPicker(selectedMood: String?, onMoodSelected: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        MoodButton(emoji = "😊", isSelected = selectedMood == "happy") {
            onMoodSelected("happy")
        }
        MoodButton(emoji = "😌", isSelected = selectedMood == "calm") {
            onMoodSelected("calm")
        }
        MoodButton(emoji = "😰", isSelected = selectedMood == "anxious") {
            onMoodSelected("anxious")
        }
        MoodButton(emoji = "😢", isSelected = selectedMood == "sad") {
            onMoodSelected("sad")
        }
        MoodButton(emoji = "😴", isSelected = selectedMood == "tired") {
            onMoodSelected("tired")
        }
    }
}

@Composable
fun MoodButton(emoji: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50) // Circle shape
            )
            .clickable { onClick() }
    ) {
        Text(text = emoji, fontSize = 18.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isCurrentEntryToday: Boolean, // ✅ Receive as parameter
    navController: NavController, // ✅ Receive NavController
    onLongClick: (() -> Unit)? = null
) {
    val isUser = message.role == "user"
    val timestamp = LocalDateTime
        .ofInstant(java.time.Instant.ofEpochMilli(message.timestamp), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a"))

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            // --- Add combinedClickable for long press ---
            .combinedClickable(
                onClick = { /* Handle normal click if needed */ },
                onLongClick = onLongClick // Assign the passed lambda
            )
        // --- End addition ---
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // --- ✅ ADD IMAGE DISPLAY HERE ---
            message.imageUri?.let { imagePath ->
                val imageUri = Uri.fromFile(File(imagePath))

                AsyncImage(
                    model = imageUri,
                    contentDescription = "Attached Image",
                    modifier = Modifier
                        .size(200.dp) // Adjust size as needed
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp)) // Optional: rounded corners
                        .clickable { // ✅ Add clickable to open full-size view
                            // Navigate to ImageViewerScreen
                            navController.navigate("image_viewer/${imagePath}") // Pass the image path
                        },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground), // Optional: placeholder
                    error = painterResource(R.drawable.ic_launcher_foreground) // Optional: error state
                )
            }
            // --- ✅ END IMAGE DISPLAY ---

            // Display text content
            if (message.content.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp) // Horizontal padding for bubble
                ) {
                    Text(
                        text = message.content,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp), // Standard bubble padding
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Display timestamp
            Text(
                text = timestamp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp, start = if (isUser) 0.dp else 12.dp, end = if (isUser) 12.dp else 0.dp) // Align timestamp with bubble start
            )
        }
    }
}

// --- Main ChatInputScreen Composable ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatInputScreen(
    viewModel: JournalViewModel,
    navController: NavController // ✅ Add NavController parameter
) {
    val entry = viewModel.currentEntry
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    // Remember the LazyListState to control scrolling
    val listState = rememberLazyListState()

    // --- State for Long Press Handling ---
    var messageActionMenuForId by remember { mutableStateOf<String?>(null) }
    var editTextValue by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // --- End Long Press State ---

    // --- State for Media Picker ---
    var showMediaPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var tempImageFile by remember { mutableStateOf<File?>(null) } // To hold the temporary image file for camera
    var imageUri by remember { mutableStateOf<Uri?>(null) } // To hold the selected image URI
    // --- End Media Picker State ---

    Column(modifier = Modifier.fillMaxSize()) {
        // Mood Picker Row
        MoodPicker(
            selectedMood = entry.mood,
            onMoodSelected = { viewModel.updateMood(it) }
        )

        // Journal Header
        Text(
            text = "Journal • ${entry.localDate}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Messages List - Takes available space
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f) // Takes remaining vertical space
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            reverseLayout = false // Keeps newest at the bottom
        ) {
            items(entry.messages) { message ->
                ChatBubble(
                    message = message,
                    isCurrentEntryToday = viewModel.isCurrentEntryToday, // Pass the flag
                    navController = navController, // ✅ Pass NavController
                    onLongClick = {
                        messageActionMenuForId = message.id
                        editTextValue = message.content
                    }
                )
            }
        }

        // Automatically scroll to the bottom when messages change
        LaunchedEffect(entry.messages) {
            if (entry.messages.isNotEmpty()) {
                val lastIndex = entry.messages.lastIndex
                val isNearBottom = listState.firstVisibleItemIndex >= lastIndex - 3 // Adjust threshold
                if (isNearBottom) {
                    listState.animateScrollToItem(lastIndex)
                }
            }
        }

        // Improved Input Area - Fixed at the bottom
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    textStyle = TextStyle(fontSize = 16.sp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .heightIn(min = 56.dp, max = 120.dp),
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text("Type a message...") },
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        val text = textFieldValue.text.trim()
                        if (text.isNotBlank() || imageUri != null) {
                            viewModel.addMessageWithImage(text, imageUri?.toString())
                            textFieldValue = TextFieldValue("")
                            imageUri = null
                            tempImageFile = null
                        }
                    },
                    enabled = textFieldValue.text.isNotBlank() || imageUri != null,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (textFieldValue.text.isNotBlank() || imageUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = if (textFieldValue.text.isNotBlank() || imageUri != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Media Attachment Button
                IconButton(
                    onClick = { showMediaPicker = true }, // Show the media picker dialog
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Image, // You can also use Icons.Default.AttachFile for a more generic icon
                        contentDescription = "Attach Image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // --- Edit Dialog ---
    if (showEditDialog) {
        val messageToEdit = entry.messages.find { it.id == messageActionMenuForId }
        messageToEdit?.let { msg ->
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Message") },
                text = {
                    OutlinedTextField(
                        value = editTextValue,
                        onValueChange = { editTextValue = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Trim the edited text before saving
                            val trimmedContent = editTextValue.trim()
                            if (trimmedContent.isNotBlank()) {
                                viewModel.editMessage(msg.id, trimmedContent)
                            } else {
                                // If content is blank, treat as delete
                                viewModel.deleteMessage(msg.id)
                            }
                            showEditDialog = false
                            messageActionMenuForId = null
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    // --- End Edit Dialog ---

    // --- Delete Confirmation Dialog ---
    if (showDeleteDialog) {
        val messageToDelete = entry.messages.find { it.id == messageActionMenuForId }
        messageToDelete?.let { msg ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Message") },
                text = { Text("Are you sure you want to delete this message?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMessage(msg.id)
                            showDeleteDialog = false
                            messageActionMenuForId = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    // --- End Delete Confirmation Dialog ---

    // --- Activity Result Launchers ---
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempImageFile != null) {
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempImageFile!!
            )
            imageUri = photoURI
        } else {
            tempImageFile = null
        }
    }
// --- End Launchers ---

    // --- Action Menu (Bottom Sheet) ---
    messageActionMenuForId?.let { messageId ->
        val messageToActOn = entry.messages.find { it.id == messageId }
        messageToActOn?.let { msg ->
            ModalBottomSheet(
                onDismissRequest = { messageActionMenuForId = null },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Edit") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                // Pre-fill the edit dialog text field
                                editTextValue = msg.content
                                showEditDialog = true
                                messageActionMenuForId = null // Close the sheet
                            }
                        )
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Delete") },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                showDeleteDialog = true
                                messageActionMenuForId = null // Close the sheet
                            }
                        )
                    )
                }
            }
        }
    }
    // --- End Action Menu ---

    // --- Media Picker Dialog ---
    if (showMediaPicker) {
        AlertDialog(
            onDismissRequest = { showMediaPicker = false },
            title = { Text("Add Media") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Take Photo Option
                    TextButton(
                        onClick = {
                            // Create temp file and launch camera
                            tempImageFile = createTempImageFile(context)
                            tempImageFile?.let { file ->
                                val photoURI: Uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                takePictureLauncher.launch(photoURI)
                            }
                            showMediaPicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Take Photo")
                        }
                    }
                    Divider()
                    // Choose from Gallery Option
                    TextButton(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                            showMediaPicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Choose from Gallery")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    // --- End Media Picker Dialog ---
}

// --- Utility Functions ---

fun createTempImageFile(context: Context): File {
    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}

// Function to save image to app-private directory
fun saveImageToPrivateDir(context: Context, imageUri: Uri): File {
    val fileName = "image_${System.currentTimeMillis()}.jpg"
    val privateDir = context.filesDir // App-specific private directory
    val outputFile = File(privateDir, fileName)

    try {
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        // Handle error (e.g., show Snackbar)
    }

    return outputFile
}