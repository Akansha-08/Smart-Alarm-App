package com.example.smartalarmapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartalarmapp.data.Alarm
import com.example.smartalarmapp.viewmodel.AlarmViewModel
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onAddAlarm: () -> Unit,
    onEditAlarm: (Alarm) -> Unit,          // new callback for editing
    viewModel: AlarmViewModel = viewModel()
) {
    // collect StateFlow as Compose State - recomposes when list changes
    val alarms by viewModel.alarms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart Alarm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        // FAB to add new alarm
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlarm) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { paddingValues ->

        if (alarms.isEmpty()) {
            // show empty state when no alarms exist
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No alarms yet!\nTap + to add one",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // scrollable list of alarm cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp), // gap between cards
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onEdit = { onEditAlarm(alarm) }   // pass edit callback
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit                     // new edit callback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // convert 24hr to 12hr format with AM/PM
                val displayHour = when {
                    alarm.hour == 0 -> 12           // midnight = 12 AM
                    alarm.hour > 12 -> alarm.hour - 12  // PM hours
                    else -> alarm.hour              // 1-12 AM
                }
                val amPm = if (alarm.hour < 12) "AM" else "PM"

                Text(
                    text = "%02d:%02d %s".format(displayHour, alarm.minute, amPm),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                // show label only if it's not empty
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // show repeat label - "Rings once" if no days, else show selected days
                Text(
                    text = if (alarm.repeatDays == "Once") "Rings once"
                    else "Repeats: ${alarm.repeatDays}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.repeatDays == "Once")
                        MaterialTheme.colorScheme.onSurfaceVariant  // grey for once
                    else
                        MaterialTheme.colorScheme.primary            // blue for repeat
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Alarm",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // toggle switch to enable/disable alarm
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                // delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}