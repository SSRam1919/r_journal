package com.baverika.r_journal.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baverika.r_journal.ui.viewmodel.YearInPixelsViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// Mood Colors
val MoodColors = mapOf(
    "Happy" to Color(0xFFFFD54F),     // Amber
    "Calm" to Color(0xFF4DB6AC),      // Teal
    "Anxious" to Color(0xFFBA68C8),   // Purple (was Stressed)
    "Sad" to Color(0xFF64B5F6),       // Blue
    "Tired" to Color(0xFF90A4AE)      // Blue Grey (was Bored)
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun YearInPixelsScreen(
    viewModel: YearInPixelsViewModel,
    navController: androidx.navigation.NavController
) {
    val moodMap by viewModel.moodMap.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    
    // State for mood selection dialog
    var showMoodDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Year in Pixel $selectedYear") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Header: Month Initials
            Row(modifier = Modifier.fillMaxWidth()) {
                // Spacer for day numbers column
                Spacer(modifier = Modifier.width(24.dp))
                
                // Months J F M ...
                for (month in 1..12) {
                    Text(
                        text = java.time.Month.of(month).getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            // Check if we should render grid or loop manually for easier alignment with rows
            // 31 Rows (Days)
            // Each Row has: DayNum + 12 Month Cells
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                for (day in 1..31) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp), // Fixed height per row
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day Number
                        Text(
                            text = "$day",
                            modifier = Modifier.width(24.dp),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.End
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // 12 Columns (Months)
                        for (month in 1..12) {
                            val currentDate = try {
                                LocalDate.of(selectedYear, month, day)
                            } catch (_: Exception) {
                                null // Invalid date (e.g., Feb 30)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(1.dp)
                                    .fillMaxHeight()
                            ) {
                                if (currentDate != null) {
                                    val mood = moodMap[currentDate]
                                    val color = MoodColors[mood] ?: Color.LightGray.copy(alpha = 0.2f)
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, MaterialTheme.shapes.extraSmall)
                                            .clickable {
                                                selectedDate = currentDate
                                                showMoodDialog = true
                                            }
                                    )
                                } else {
                                    // Empty box for invalid dates
                                    Spacer(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Legend
                Text("Legend", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Flexible Grid for Legend
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodColors.forEach { (mood, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(mood, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Mood Selection Dialog
    if (showMoodDialog && selectedDate != null) {
        AlertDialog(
            onDismissRequest = { showMoodDialog = false },
            title = { 
                Text(
                    text = selectedDate!!.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium
                ) 
            },
            text = {
                Column {
                    Text("How was your day?")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MoodColors.toList().size) { index ->
                            val (mood, color) = MoodColors.toList()[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setMood(selectedDate!!, mood)
                                        showMoodDialog = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(color, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Text(mood, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoodDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// Helper Composable for FlowLayout
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
