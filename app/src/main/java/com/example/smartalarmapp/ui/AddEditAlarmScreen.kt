package com.example.smartalarmapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartalarmapp.data.Alarm
import com.example.smartalarmapp.viewmodel.AlarmViewModel
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TimeInput
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    onNavigateBack: () -> Unit,          // callback to go back to list
    alarmToEdit: Alarm? = null,            // null means adding new, non-null means editing
    viewModel: AlarmViewModel = viewModel()
) {
    // local UI state for form fields
    // time picker state - starts at 7:00 AM by default
    // pre-fill values from existing alarm if editing, else use defaults
    val timePickerState = rememberTimePickerState(
        initialHour = alarmToEdit?.hour ?: 7,
        initialMinute = alarmToEdit?.minute ?: 0,
        is24Hour = false
    )
    var showTimePicker by remember { mutableStateOf(false) }
    var showManualInput by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "") }
    var stepCount by remember { mutableStateOf(alarmToEdit?.stepCount?.toString() ?: "10") }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val allDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
    // pre-select days if editing existing alarm
    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (alarmToEdit?.repeatDays != null && alarmToEdit.repeatDays != "Once") {
                addAll(alarmToEdit.repeatDays.split(","))  // split stored string back to list
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmToEdit != null) "Edit Alarm" else "Add Alarm") },
                navigationIcon = {
                    // back button
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // time display and picker button
            Text("Set Time", style = MaterialTheme.typography.titleMedium)

            // shows currently selected time, tap to open picker
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "%02d:%02d %s".format(
                        if (timePickerState.is24hour) timePickerState.hour
                        else if (timePickerState.hour % 12 == 0) 12
                        else timePickerState.hour % 12,
                        timePickerState.minute,
                        if (timePickerState.hour < 12) "AM" else "PM"
                    ),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // time picker dialog
            if (showTimePicker) {
                Dialog(onDismissRequest = { showTimePicker = false }) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Select Time",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (showManualInput) {
                                // manual keyboard input mode
                                TimeInput(state = timePickerState)
                            } else {
                                // clock dial mode
                                TimePicker(state = timePickerState)
                            }

                            // toggle button between clock and keyboard
                            TextButton(
                                onClick = { showManualInput = !showManualInput }
                            ) {
                                Text(
                                    // switch label based on current mode
                                    if (showManualInput) "Switch to Clock" else "Enter time manually"
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showTimePicker = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = { showTimePicker = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }
            }

            // optional label field
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // step count required to dismiss alarm
            OutlinedTextField(
                value = stepCount,
                onValueChange = { stepCount = it },
                label = { Text("Steps to dismiss") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // repeat days selector
            Text("Repeat", style = MaterialTheme.typography.titleMedium)

            // days row with wrap support
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                days.forEach { day ->
                    // toggle chip for each day
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            if (selectedDays.contains(day)) selectedDays.remove(day)
                            else selectedDays.add(day)
                        },
                        label = { Text(day, fontSize = 10.sp) }
                    )
                }
            }
            // everyday shortcut button
            FilterChip(
                selected = selectedDays.containsAll(days),  // true if all days selected
                onClick = {
                    if (selectedDays.containsAll(days)) {
                        selectedDays.clear()                // deselect all if already all selected
                    } else {
                        selectedDays.clear()
                        selectedDays.addAll(days)           // select all days at once
                    }
                },
                label = { Text("Everyday", fontSize = 11.sp) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // save button at bottom
            Button(
                onClick = {
                    val h = timePickerState.hour
                    val m = timePickerState.minute
                    val steps = stepCount.toIntOrNull() ?: 10
                    viewModel.saveAlarm(
                        Alarm(
                            id = alarmToEdit?.id ?: 0,   // keep same ID if editing
                            hour = h,
                            minute = m,
                            label = label,
                            isEnabled = alarmToEdit?.isEnabled ?: true,
                            repeatDays = if (selectedDays.isEmpty()) "Once"
                            else selectedDays.joinToString(","),
                            stepCount = steps
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Alarm", fontSize = 16.sp)
            }
        }
    }
}