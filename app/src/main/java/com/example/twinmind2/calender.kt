package com.example.twinmind2

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat



data class CalendarEvent(
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long
)

@Composable

fun CalendarEventCard(event: CalendarEvent) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Card background is white
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${timeFormat.format(Date(event.startTime))} - ${
                    timeFormat.format(Date(event.endTime))
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 5. Screen composable
@Composable
fun CalendarEventScreen(viewModel: AuthViewModel) {
    val events by viewModel.events.collectAsState()
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            viewModel.loadEvents()
        } else {
            showRationale = true
        }
    }

    LaunchedEffect(Unit) {
        val currentStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        permissionGranted = currentStatus
        if (!currentStatus) {
            launcher.launch(Manifest.permission.READ_CALENDAR)
        } else {
            viewModel.loadEvents()
        }
    }

    when {
        permissionGranted -> {

            if (events.isEmpty()) {
                Text(
                    "No events found",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                viewModel.groupEventsByDate(events)
                val grouped by viewModel.eventsgroup.collectAsState()
                LazyColumn {
                    grouped.forEach { (date, dayEvents) ->
                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 20.dp, start = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(dayEvents) { event ->
                            CalendarEventCard(event)
                        }
                    }
                }
            }
        }

        showRationale -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Calendar access is required to display events")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.READ_CALENDAR) }) {
                    Text("Grant Permission")
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Requesting calendar permission...")
            }
        }
    }
}


