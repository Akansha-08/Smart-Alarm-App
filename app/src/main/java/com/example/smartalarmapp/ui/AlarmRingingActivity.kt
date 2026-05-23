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
import android.os.Build
import android.view.WindowManager
import android.app.NotificationManager
import android.content.Context

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // force screen on and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // keep screen on while ringing
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        // get alarm id first - needed for notification cancellation
        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        // cancel the notification that launched this activity
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId)

        // get alarm details passed from AlarmReceiver
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val stepCount = intent.getIntExtra("STEP_COUNT", 10)

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