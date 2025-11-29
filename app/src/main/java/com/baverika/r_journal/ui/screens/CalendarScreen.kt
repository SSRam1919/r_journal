package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.baverika.r_journal.repository.JournalRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    journalRepo: JournalRepository,
    navController: NavController
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val entries by journalRepo.allEntrySummaries.collectAsState(initial = emptyList())

    // Map of Date -> Entry ID (for quick lookup)
    val entryMap = remember(entries) {
        entries.associateBy { it.localDate }.mapValues { it.value.id }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        // Days of Week Header
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0=Sun, 1=Mon... (adjust based on locale if needed, assuming Sun start here)
        
        // Simple adjustment for Sunday start (Java DayOfWeek 1=Mon, 7=Sun)
        // If we want Sunday to be 0:
        // Mon(1) -> 1, Tue(2) -> 2 ... Sun(7) -> 0
        val startOffset = if (currentMonth.atDay(1).dayOfWeek.value == 7) 0 else currentMonth.atDay(1).dayOfWeek.value

        val totalSlots = daysInMonth + startOffset
        val daysList = (1..totalSlots).map { 
            if (it <= startOffset) null else it - startOffset 
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(daysList) { day ->
                if (day != null) {
                    val date = currentMonth.atDay(day)
                    val entryId = entryMap[date]
                    val isToday = date == LocalDate.now()

                    CalendarDay(
                        day = day,
                        isToday = isToday,
                        hasEntry = entryId != null,
                        onClick = {
                            if (entryId != null) {
                                navController.navigate("chat_input/$entryId")
                            } else if (isToday) {
                                navController.navigate("chat_input") // Create/Open today
                            }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isToday: Boolean,
    hasEntry: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer
                else if (hasEntry) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else Color.Transparent
            )
            .clickable(enabled = hasEntry || isToday, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            
            if (hasEntry) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
