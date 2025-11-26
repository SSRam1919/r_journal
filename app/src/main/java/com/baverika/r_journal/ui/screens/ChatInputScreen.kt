package com.baverika.r_journal.ui.screens

// Kotlin / stdlib / coroutines
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// Android / Core / Navigation
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController

// Compose runtime & foundation
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState

// Compose UI / graphics / resources
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Coil
import coil.compose.AsyncImage

// Activity result APIs (missing earlier)
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// Material1 small imports REMOVED

// Material3 (primary UI) - alias Text/Icon to avoid ambiguity
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator

// Experimental annotation import
import androidx.compose.material3.ExperimentalMaterial3Api

// App classes
import com.baverika.r_journal.R
import com.baverika.r_journal.data.local.entity.ChatMessage
import com.baverika.r_journal.ui.viewmodel.JournalViewModel

// Icons (shared) - using material icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
            M3Text(
                text = "Mood:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )

            val AVAILABLE_MOODS = listOf(
                "happy" to "\uD83D\uDE0A",
                "calm" to "\uD83D\uDE0C",
                "anxious" to "\uD83D\uDE30",
                "sad" to "\uD83D\uDE22",
                "tired" to "\uD83D\uDE34"
            )

            AVAILABLE_MOODS.forEach { (mood, emoji) ->
                val isSelected = mood in selectedMoods
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                androidx.compose.foundation.layout.Box(
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
                    M3Text(text = emoji, fontSize = 22.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            if (selectedMoods.isNotEmpty() && canEdit) {
                M3Text(
                    text = "${selectedMoods.size}/3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (!canEdit) {
                M3Text(
                    text = "Past entry",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isCurrentEntryToday: Boolean,
    isAddedLater: Boolean,
    navController: NavController,
    onLongClick: (() -> Unit)? = null,
    repliedMessage: ChatMessage? = null,
    onQuoteClick: (() -> Unit)? = null,
    isHighlighted: Boolean = false

) {
    val isUser = message.role == "user"
    val timestamp = LocalDateTime
        .ofInstant(java.time.Instant.ofEpochMilli(message.timestamp), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a"))

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Row(
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
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
                if (isAddedLater) {
                    M3Text(
                        text = "Added ${LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(message.timestamp), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                // quoted reply (if any)
                repliedMessage?.let { original ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth(0.82f)
                            .clickable { onQuoteClick?.invoke() }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .background(
                                        if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                M3Text(
                                    text = if (original.role == "user") "You" else original.role.replaceFirstChar { it.uppercaseChar() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                M3Text(
                                    text = original.content.ifBlank { "[Image]" },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                // image
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

                // text
                if (message.content.isNotBlank()) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        M3Text(
                            text = message.content,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // timestamp
                M3Text(
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

    val hasUnsavedText = textFieldValue.text.trim().isNotEmpty() || imageUri != null

    var showExitConfirmation by remember { mutableStateOf(false) }
    var messageActionMenuForId by remember { mutableStateOf<String?>(null) }
    var editTextValue by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showMediaPicker by remember { mutableStateOf(false) }
    var tempImageFile by remember { mutableStateOf<File?>(null) }

    // reply state & coroutine scope
    var replyToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // inside ChatInputScreen near other remembers
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var highlightedMessageId by remember { mutableStateOf<String?>(null) }



    // launchers
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
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

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Top Bar
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
                M3Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Column(modifier = Modifier.weight(1f)) {
                M3Text(
                    text = if (isCurrentEntryToday) "Today's Journal" else "Journal Entry",
                    style = MaterialTheme.typography.titleLarge
                )
                M3Text(
                    text = entry.localDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            M3Text(
                text = "${entry.messages.size}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        CompactMoodPicker(
            selectedMoods = selectedMoods,
            onMoodToggle = { mood -> viewModel.toggleMood(mood) },
            canEdit = canEditMood
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        // Messages list
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (entry.messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    M3Icon(imageVector = Icons.Default.Create, contentDescription = null, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    M3Text(
                        text = if (isCurrentEntryToday) "Start writing..." else "No entries yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    M3Text(
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
                    itemsIndexed(entry.messages, key = { _, it -> it.id }) { index, message ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { newValue ->
                                newValue == SwipeToDismissBoxValue.StartToEnd
                            }
                        )

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                                replyToMessage = message

                                // haptic + snackbar feedback
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Replying to message")
                                }

                                dismissState.reset()
                            }
                        }

                        val replied = entry.messages.find { it.id == message.replyToMessageId }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                // show icon only while the user is swiping (fraction > small threshold)
                                val fraction = dismissState.progress
                                if (fraction > 0.05f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(start = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            M3Icon(imageVector = Icons.Default.Reply, contentDescription = "Reply", modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = false,
                            content = {
                                ChatBubble(
                                    message = message,
                                    isCurrentEntryToday = isCurrentEntryToday,
                                    isAddedLater = viewModel.isMessageAddedLater(message),
                                    navController = navController,
                                    onLongClick = {
                                        messageActionMenuForId = message.id
                                        editTextValue = message.content
                                    },
                                    repliedMessage = replied,
                                    isHighlighted = (message.id == highlightedMessageId),
                                    onQuoteClick = {
                                        val targetIndex = entry.messages.indexOfFirst { it.id == message.replyToMessageId }
                                        if (targetIndex >= 0) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(targetIndex)
                                                // set highlight for a short time
                                                highlightedMessageId = entry.messages[targetIndex].id
                                                // clear after delay
                                                kotlinx.coroutines.delay(1500)
                                                highlightedMessageId = null
                                            }
                                        }
                                    }
                                )
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

        // Input area
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Reply preview
                replyToMessage?.let { replying ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                M3Text(text = "Replying", style = MaterialTheme.typography.labelSmall)
                                M3Text(text = replying.content.ifBlank { "[Image]" }, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                            IconButton(onClick = { replyToMessage = null }) {
                                M3Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel reply")
                            }
                        }
                    }
                }

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
                                M3Icon(imageVector = Icons.Default.Close, contentDescription = "Remove")
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
                            M3Text(
                                if (isCurrentEntryToday) "Type a message..."
                                else "Add a reflection..."
                            )
                        },
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = { showMediaPicker = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            )
                    ) {
                        M3Icon(imageVector = Icons.Default.Image, contentDescription = "Attach Image")
                    }

                    Spacer(Modifier.width(8.dp))

                    val isEnabled = textFieldValue.text.isNotBlank() || imageUri != null
                    IconButton(
                        onClick = {
                            val text = textFieldValue.text.trim()
                            if (text.isNotBlank() || imageUri != null) {
                                viewModel.addMessageWithImage(text, imageUri?.toString(), replyTo = replyToMessage)
                                textFieldValue = TextFieldValue("")
                                imageUri = null
                                tempImageFile = null
                                replyToMessage = null
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
                        M3Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog) {
        val messageToEdit = entry.messages.find { it.id == messageActionMenuForId }
        messageToEdit?.let {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    messageActionMenuForId = null
                },
                title = { M3Text("Edit Message") },
                text = {
                    OutlinedTextField(
                        value = editTextValue,
                        onValueChange = { editTextValue = it },
                        label = { M3Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                },
                confirmButton = {
                    androidx.compose.material.TextButton(
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
                        M3Text("Save")
                    }
                },
                dismissButton = {
                    androidx.compose.material.TextButton(onClick = {
                        showEditDialog = false
                        messageActionMenuForId = null
                    }) {
                        M3Text("Cancel")
                    }
                }
            )
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        val messageToDelete = entry.messages.find { it.id == messageActionMenuForId }
        messageToDelete?.let {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    messageActionMenuForId = null
                },
                title = { M3Text("Delete Message") },
                text = { M3Text("Are you sure you want to delete this message?") },
                confirmButton = {
                    androidx.compose.material.TextButton(
                        onClick = {
                            viewModel.deleteMessage(it.id)
                            showDeleteDialog = false
                            messageActionMenuForId = null
                        }
                    ) {
                        M3Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    androidx.compose.material.TextButton(onClick = {
                        showDeleteDialog = false
                        messageActionMenuForId = null
                    }) {
                        M3Text("Cancel")
                    }
                }
            )
        }
    }

    // Action menu bottom sheet
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
                        headlineContent = { M3Text("Edit") },
                        leadingContent = { M3Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable {
                            editTextValue = it.content
                            showEditDialog = true
                            messageActionMenuForId = null
                        }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { M3Text("Delete") },
                        leadingContent = { M3Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showDeleteDialog = true
                            messageActionMenuForId = null
                        }
                    )
                }
            }
        }
    }

    // Media picker dialog
    if (showMediaPicker) {
        AlertDialog(
            onDismissRequest = { showMediaPicker = false },
            title = { M3Text("Add Media") },
            text = {
                Column {
                    androidx.compose.material.TextButton(
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
                        M3Icon(imageVector = Icons.Default.Camera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        M3Text("Take Photo")
                    }
                    Divider()
                    androidx.compose.material.TextButton(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                            showMediaPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        M3Icon(imageVector = Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        M3Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {
                androidx.compose.material.TextButton(onClick = { showMediaPicker = false }) {
                    M3Text("Cancel")
                }
            }
        )
    }

    // Exit confirmation
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { M3Text("Discard Unsaved Changes?") },
            text = { M3Text("You have unsaved text. Are you sure you want to leave?") },
            confirmButton = {
                androidx.compose.material.TextButton(
                    onClick = {
                        showExitConfirmation = false
                        navController.popBackStack()
                    }
                ) {
                    M3Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material.TextButton(onClick = { showExitConfirmation = false }) {
                    M3Text("Keep Writing")
                }
            }
        )
    }
}

// helper
fun createTempImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
        .format(java.util.Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}
