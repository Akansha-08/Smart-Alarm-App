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
import com.example.smartalarmapp.data.AlarmDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get alarm details passed from AlarmReceiver
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val stepCount = intent.getIntExtra("STEP_COUNT", 10)

        // check if alarm was disabled before it could be cancelled
        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        CoroutineScope(Dispatchers.IO).launch {
            val dao = AlarmDatabase.getDatabase(this@AlarmRingingActivity).alarmDao()
            val alarm = dao.getAlarmById(alarmId)

            // if alarm is disabled, just finish this activity immediately
            if (alarm == null || !alarm.isEnabled) {
                finish()
                return@launch
            }
        }

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