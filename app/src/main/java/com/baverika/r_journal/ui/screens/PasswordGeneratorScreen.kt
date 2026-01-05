package com.baverika.r_journal.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baverika.r_journal.data.local.entity.Password
import com.baverika.r_journal.ui.viewmodel.PasswordViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordViewModel
) {
    val passwords by viewModel.passwords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var siteName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    // Generator State - Industry standard defaults
    var lowerCount by remember { mutableStateOf(4) }
    var upperCount by remember { mutableStateOf(4) }
    var numberCount by remember { mutableStateOf(2) }
    var specialCount by remember { mutableStateOf(2) }

    var generatedPassword by remember { mutableStateOf("") }
    var isGeneratorExpanded by remember { mutableStateOf(true) }

    // Regenerate password when counts change
    LaunchedEffect(lowerCount, upperCount, numberCount, specialCount) {
        generatedPassword = generatePassword(lowerCount, upperCount, numberCount, specialCount)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search passwords...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }

        // Generator Card (Collapsible)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGeneratorExpanded = !isGeneratorExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Password Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            if (isGeneratorExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isGeneratorExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Input Fields - Compact Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = siteName,
                                onValueChange = { siteName = it },
                                label = { Text("Site", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("User", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Compact Scroll Wheels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompactWheelPicker(
                                label = "a-z",
                                value = lowerCount,
                                onValueChange = { lowerCount = it }
                            )
                            CompactWheelPicker(
                                label = "A-Z",
                                value = upperCount,
                                onValueChange = { upperCount = it }
                            )
                            CompactWheelPicker(
                                label = "0-9",
                                value = numberCount,
                                onValueChange = { numberCount = it }
                            )
                            CompactWheelPicker(
                                label = "#@!",
                                value = specialCount,
                                onValueChange = { specialCount = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Generated Password Display
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = generatedPassword.ifEmpty { "—" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        generatedPassword = generatePassword(lowerCount, upperCount, numberCount, specialCount)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save Button
                        FilledTonalButton(
                            onClick = {
                                if (siteName.isNotBlank() && username.isNotBlank() && generatedPassword.isNotBlank()) {
                                    viewModel.addPassword(siteName.trim(), username.trim(), generatedPassword)
                                    siteName = ""
                                    username = ""
                                    generatedPassword = generatePassword(lowerCount, upperCount, numberCount, specialCount)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = siteName.isNotBlank() && username.isNotBlank() && generatedPassword.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Saved Passwords Header
        if (passwords.isNotEmpty()) {
            item {
                Text(
                    "Saved (${passwords.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Password List
        items(passwords, key = { it.id }) { password ->
            PasswordListItem(
                password = password,
                onDelete = { viewModel.deletePassword(password) }
            )
        }

        // Empty State
        if (passwords.isEmpty() && searchQuery.isBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No passwords saved yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun CompactWheelPicker(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 0..12
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        VerticalWheelPicker(
            items = range.toList(),
            initialIndex = value.coerceIn(range),
            onValueChange = onValueChange,
            itemHeight = 28.dp,
            visibleItems = 3,
            width = 48.dp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerticalWheelPicker(
    items: List<Int>,
    initialIndex: Int,
    onValueChange: (Int) -> Unit,
    itemHeight: Dp,
    visibleItems: Int,
    width: Dp
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val fadingEdgeHeight = itemHeight

    // Track scroll and update value
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                val newValue = items.getOrNull(index) ?: items.first()
                onValueChange(newValue)
            }
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(itemHeight * visibleItems)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        // Fading edges effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    // Top fade
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.3f to Color.Black,
                            0.7f to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight),
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    val isSelected = listState.firstVisibleItemIndex == index
                    
                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Center indicator
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(width - 8.dp)
                .height(itemHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )
    }
}

@Composable
private fun PasswordListItem(
    password: Password,
    onDelete: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { 
                Text(
                    "Delete Password?",
                    style = MaterialTheme.typography.titleMedium
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to delete the password for \"${password.siteName}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = password.siteName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = password.username,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isVisible) password.passwordValue else "•".repeat(password.passwordValue.length.coerceAtMost(16)),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = { isVisible = !isVisible }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(password.passwordValue))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun generatePassword(lower: Int, upper: Int, numbers: Int, special: Int): String {
    val lowerChars = "abcdefghijklmnopqrstuvwxyz"
    val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val numberChars = "0123456789"
    val specialChars = "!@#$%^&*_+-=" // Widely accepted special chars

    val sb = StringBuilder()

    repeat(lower) { sb.append(lowerChars.random()) }
    repeat(upper) { sb.append(upperChars.random()) }
    repeat(numbers) { sb.append(numberChars.random()) }
    repeat(special) { sb.append(specialChars.random()) }

    if (sb.isEmpty()) return ""

    val list = sb.toString().toMutableList()
    list.shuffle()
    return list.joinToString("")
}
