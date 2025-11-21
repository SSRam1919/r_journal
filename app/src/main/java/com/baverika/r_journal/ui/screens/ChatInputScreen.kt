// app/src/main/java/com/baverika/r_journal/ui/screens/ChatInputScreen.kt

package com.baverika.r_journal.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.baverika.r_journal.R
import com.baverika.r_journal.data.local.entity.ChatMessage
import com.baverika.r_journal.ui.viewmodel.JournalViewModel
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Available moods with emoji representations
private val AVAILABLE_MOODS = listOf(
    "happy" to "\uD83D\uDE0A",      // 😊
    "calm" to "\uD83D\uDE0C",       // 😌
    "anxious" to "\uD83D\uDE30",    // 😰
    "sad" to "\uD83D\uDE22",        // 😢
    "tired" to "\uD83D\uDE34"       // 😴
)

@Composable
fun CompactMoodPicker(
    selectedMoods: Set<String>,
    onMoodToggle: (String) -> Unit,
    canEdit: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mood:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )

            AVAILABLE_MOODS.forEach { (mood, emoji) ->
                val isSelected = mood in selectedMoods
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(scale)
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(enabled = canEdit) { onMoodToggle(mood) }
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            if (selectedMoods.isNotEmpty() && canEdit) {
                Text(
                    text = "${selectedMoods.size}/3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (!canEdit) {
                Text(
                    text = "Past entry",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isCurrentEntryToday: Boolean,
    isAddedLater: Boolean,
    navController: NavController,
    onLongClick: (() -> Unit)? = null
) {
    val isUser = message.role == "user"
    val timestamp = LocalDateTime
        .ofInstant(java.time.Instant.ofEpochMilli(message.timestamp), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a"))

    // Animate message appearance
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Row(
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .then(
                    if (onLongClick != null) {
                        Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = onLongClick
                        )
                    } else Modifier
                )
        ) {
            Column(
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                // Show "Added later" label
                if (isAddedLater) {
                    Text(
                        text = "Added ${LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(message.timestamp),
                            ZoneId.systemDefault()
                        ).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                // Image attachment
                message.imageUri?.let { imagePath ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .size(200.dp)
                            .clickable {
                                val encodedPath = URLEncoder.encode(
                                    imagePath,
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate("image_viewer/$encodedPath")
                            },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        AsyncImage(
                            model = Uri.fromFile(File(imagePath)),
                            contentDescription = "Attached Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_launcher_foreground),
                            error = painterResource(R.drawable.ic_launcher_foreground)
                        )
                    }
                }

                // Text content
                if (message.content.isNotBlank()) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = message.content,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Timestamp
                Text(
                    text = timestamp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(
                        top = 2.dp,
                        start = if (isUser) 0.dp else 12.dp,
                        end = if (isUser) 12.dp else 0.dp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatInputScreen(
    viewModel: JournalViewModel,
    navController: NavController
) {
    val entry = viewModel.currentEntry
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedMoods = viewModel.getSelectedMoods()
    val canEditMood = viewModel.canEditMood
    val isCurrentEntryToday = viewModel.isCurrentEntryToday

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Track if there's unsaved text
    val hasUnsavedText = textFieldValue.text.trim().isNotEmpty() || imageUri != null

    // Show confirmation dialog when navigating back with unsaved text
    var showExitConfirmation by remember { mutableStateOf(false) }

    // State for long press handling
    var messageActionMenuForId by remember { mutableStateOf<String?>(null) }
    var editTextValue by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // State for media picker
    var showMediaPicker by remember { mutableStateOf(false) }
    var tempImageFile by remember { mutableStateOf<File?>(null) }


    // Camera launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
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


    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempImageFile = createTempImageFile(context)
            tempImageFile?.let { file ->
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                takePictureLauncher.launch(photoURI)
            }
        }
    }

    // Image picker launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }




    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (hasUnsavedText) {
                    showExitConfirmation = true
                } else {
                    navController.popBackStack()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentEntryToday) "Today's Journal" else "Journal Entry",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = entry.localDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${entry.messages.size}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Compact Mood Picker SECOND
        CompactMoodPicker(
            selectedMoods = selectedMoods,
            onMoodToggle = { mood -> viewModel.toggleMood(mood) },
            canEdit = canEditMood
        )

        Divider()

        // Messages List
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (entry.messages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCurrentEntryToday) "Start writing..." else "No entries yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isCurrentEntryToday) "What's on your mind?" else "Add a reflection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(entry.messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            isCurrentEntryToday = isCurrentEntryToday,
                            isAddedLater = viewModel.isMessageAddedLater(message),
                            navController = navController,
                            onLongClick = {
                                // Only allow edit/delete for today's messages
                                if (isCurrentEntryToday && !viewModel.isMessageAddedLater(message)) {
                                    messageActionMenuForId = message.id
                                    editTextValue = message.content
                                }
                            }
                        )
                    }
                }
            }
        }

        // Auto-scroll to bottom when new messages added
        LaunchedEffect(entry.messages.size) {
            if (entry.messages.isNotEmpty()) {
                listState.animateScrollToItem(entry.messages.lastIndex)
            }
        }

        // Input Area
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Image preview
                imageUri?.let { uri ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .height(120.dp)
                            .fillMaxWidth()
                    ) {
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        CircleShape
                                    )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = {
                            Text(
                                if (isCurrentEntryToday) "Type a message..."
                                else "Add a reflection..."
                            )
                        },
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp)
                    )

                    // Attach button
                    IconButton(
                        onClick = { showMediaPicker = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Attach Image",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Send button with animation
                    val isEnabled = textFieldValue.text.isNotBlank() || imageUri != null
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
                        enabled = isEnabled,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isEnabled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (isEnabled) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        val messageToEdit = entry.messages.find { it.id == messageActionMenuForId }
        messageToEdit?.let {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    messageActionMenuForId = null
                },
                title = { Text("Edit Message") },
                text = {
                    OutlinedTextField(
                        value = editTextValue,
                        onValueChange = { editTextValue = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val trimmed = editTextValue.trim()
                            if (trimmed.isNotBlank()) {
                                viewModel.editMessage(it.id, trimmed)
                            } else {
                                viewModel.deleteMessage(it.id)
                            }
                            showEditDialog = false
                            messageActionMenuForId = null
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditDialog = false
                        messageActionMenuForId = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        val messageToDelete = entry.messages.find { it.id == messageActionMenuForId }
        messageToDelete?.let {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    messageActionMenuForId = null
                },
                title = { Text("Delete Message") },
                text = { Text("Are you sure you want to delete this message?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMessage(it.id)
                            showDeleteDialog = false
                            messageActionMenuForId = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        messageActionMenuForId = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // Action Menu (Bottom Sheet) - FIXED: Don't close immediately
    messageActionMenuForId?.let { messageId ->
        val message = entry.messages.find { it.id == messageId }
        message?.let {
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
                        leadingContent = { Icon(Icons.Default.Edit, null) },
                        modifier = Modifier.clickable {
                            editTextValue = it.content
                            showEditDialog = true
                            // Don't close sheet yet - dialog will handle it
                        }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Delete") },
                        leadingContent = { Icon(Icons.Default.Delete, null) },
                        modifier = Modifier.clickable {
                            showDeleteDialog = true
                            // Don't close sheet yet - dialog will handle it
                        }
                    )
                }
            }
        }
    }

    // Media Picker Dialog
    if (showMediaPicker) {
        AlertDialog(
            onDismissRequest = { showMediaPicker = false },
            title = { Text("Add Media") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                    tempImageFile = createTempImageFile(context)
                                    tempImageFile?.let { file ->
                                        val photoURI = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        takePictureLauncher.launch(photoURI)
                                    }
                                }
                                else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            showMediaPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    Divider()
                    TextButton(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                            showMediaPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from Gallery")
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

    // Exit confirmation dialog
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Discard Unsaved Changes?") },
            text = { Text("You have unsaved text. Are you sure you want to leave?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmation = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Keep Writing")
                }
            }
        )
    }
}

fun createTempImageFile(context: android.content.Context): File {
    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
        .format(java.util.Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}