package com.example.smartalarmapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartalarmapp.ui.AddEditAlarmScreen
import com.example.smartalarmapp.ui.AlarmListScreen
import com.example.smartalarmapp.ui.theme.SmartAlarmAppTheme
import com.example.smartalarmapp.data.Alarm
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAlarmAppTheme {
                // simple navigation state - true means we're on add screen
                var showAddScreen by remember { mutableStateOf(false) }
                var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }  // holds alarm being edited

                if (showAddScreen) {
                    AddEditAlarmScreen(
                        onNavigateBack = {
                            showAddScreen = false
                            alarmToEdit = null      // clear edit state on back
                        },
                        alarmToEdit = alarmToEdit
                    )
                } else {
                    AlarmListScreen(
                        onAddAlarm = { showAddScreen = true },
                        onEditAlarm = { alarm ->
                            alarmToEdit = alarm     // store alarm to edit
                            showAddScreen = true    // open add/edit screen
                        }
                    )
                }
            }
        }
    }
}