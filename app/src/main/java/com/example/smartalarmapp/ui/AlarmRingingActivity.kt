package com.example.smartalarmapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.smartalarmapp.ui.theme.SmartAlarmAppTheme

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get alarm details passed from AlarmReceiver
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val stepCount = intent.getIntExtra("STEP_COUNT", 10)

        setContent {
            SmartAlarmAppTheme {
                // placeholder UI - will be replaced in Step 5
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("⏰ $alarmLabel", fontSize = 32.sp)
                        Text("Walk $stepCount steps to dismiss", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}