package com.baverika.r_journal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.baverika.r_journal.repository.JournalRepository
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(journalRepo: JournalRepository) {
    var totalEntries by remember { mutableStateOf(0) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var thisMonthCount by remember { mutableStateOf(0) }

    // Collect Streak Stats
    LaunchedEffect(Unit) {
        journalRepo.allEntries.collectLatest { entries ->
            totalEntries = entries.size

            // Calculate dates list
            val entryDates = entries.map { entry ->
                java.time.Instant.ofEpochMilli(entry.dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

            currentStreak = calculateCurrentStreak(entryDates)
            longestStreak = calculateLongestStreak(entryDates)

            // Count this month
            val now = LocalDate.now()
            thisMonthCount = entries.count { entry ->
                val date = java.time.Instant.ofEpochMilli(entry.dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date.month == now.month && date.year == now.year
            }
        }
    }

    // Collect Mood Stats
    val moodStats by journalRepo.moodStats.collectAsState(initial = JournalRepository.MoodStats(0f, 0f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Enable scrolling for smaller screens
    ) {
        Text(
            "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Streak Section ---
        
        // Streak card (prominent)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$currentStreak",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = if (currentStreak == 1) "day streak" else "day streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (currentStreak > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Keep it going!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.EmojiEvents,
                label = "Best Streak",
                value = "$longestStreak",
                subtitle = if (longestStreak == 1) "day" else "days"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarMonth,
                label = "This Month",
                value = "$thisMonthCount",
                subtitle = if (thisMonthCount == 1) "entry" else "entries"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MenuBook,
                label = "Total Entries",
                value = "$totalEntries",
                subtitle = if (totalEntries == 1) "entry" else "entries"
            )
            
            // Spacer for alignment if needed, or another stat
             Spacer(modifier = Modifier.weight(1f))
        }

        // --- Mood Section ---

        Text(
            text = "Mood Insights",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )

        StatsSummaryCard(stats = moodStats)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsSummaryCard(stats: JournalRepository.MoodStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Weekly Avg", score = stats.weeklyAverage)
            StatItem(label = "Monthly Avg", score = stats.monthlyAverage)
        }
    }
}

@Composable
fun StatItem(label: String, score: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        val color = when {
            score >= 4.0f -> MaterialTheme.colorScheme.primary
            score >= 2.5f -> MaterialTheme.colorScheme.secondary
            score > 0f -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        }

        val displayScore = if (score > 0) String.format("%.1f", score) else "-"

        Text(
            text = displayScore,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
    }
}

// Calculate current streak (consecutive days from today going backward)
private fun calculateCurrentStreak(dates: List<LocalDate>): Int {
    if (dates.isEmpty()) return 0

    val sortedDates = dates.distinct().sorted().reversed() // Most recent first
    val today = LocalDate.now()

    // Check if today or yesterday has an entry
    if (!sortedDates.contains(today) && !sortedDates.contains(today.minusDays(1))) {
        return 0
    }

    var streak = 0
    var currentDate = today

    for (date in sortedDates) {
        if (date == currentDate || date == currentDate.minusDays(1)) {
            streak++
            currentDate = date.minusDays(1)
        } else {
            break
        }
    }

    return streak
}

// Calculate longest streak in history
private fun calculateLongestStreak(dates: List<LocalDate>): Int {
    if (dates.isEmpty()) return 0

    val sortedDates = dates.distinct().sorted()
    var maxStreak = 1
    var currentStreak = 1

    for (i in 1 until sortedDates.size) {
        val daysBetween = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
        if (daysBetween == 1L) {
            currentStreak++
            maxStreak = maxOf(maxStreak, currentStreak)
        } else {
            currentStreak = 1
        }
    }

    return maxStreak
}