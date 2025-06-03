package com.example.twinmind2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

// --- Main Screen ---
@Composable
fun MemoryListScreen(
    memories: List<MemoryMetaData>,
    viewModel: AuthViewModel,
    navController: NavController
) {
    val grouped = memories.sortedByDescending { it.startTime }.groupBy { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        grouped.forEach { (date, items) ->
            item {
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(items) { memory ->
                MemoryCard(memory) { a, b ->
                    viewModel.setDate(a)
                    viewModel.setSessionId(b)
                    viewModel.tittle = memory.title
                    viewModel.tdate = memory.date
                    viewModel.ttime = memory.startTime
                    navController.navigate("page")
                }
            }
        }
    }
}
@Composable
fun MemoryCard(
    memory: MemoryMetaData,
    onClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(memory.date, memory.sessionId) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
            ) {
                Text(
                    text = formatTime(memory.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
            }
            Text(
                text = formatDuration(memory.startTime, memory.endtime),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp),
                color = Color.Gray
            )
        }
    }
}
fun formatDate(date: String): String {
    // Convert "2025-05-12" to "Mon, May 12"
    return try {
        val parsed = LocalDate.parse(date)
        parsed.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    } catch (e: Exception) {
        date
    }
}

fun formatTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("h:mm a"))
}

fun formatDuration(startTime: Long, endTime: Long): String {
    val durationMillis = endTime - startTime
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
}

