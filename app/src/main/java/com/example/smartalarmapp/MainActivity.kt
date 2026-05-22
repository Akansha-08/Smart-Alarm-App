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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAlarmAppTheme {
                // simple navigation state - true means we're on add screen
                var showAddScreen by remember { mutableStateOf(false) }

                if (showAddScreen) {
                    // show add alarm screen
                    AddEditAlarmScreen(
                        onNavigateBack = { showAddScreen = false }
                    )
                } else {
                    // show alarm list screen
                    AlarmListScreen(
                        onAddAlarm = { showAddScreen = true }
                    )
                }
            }
        }
    }
}