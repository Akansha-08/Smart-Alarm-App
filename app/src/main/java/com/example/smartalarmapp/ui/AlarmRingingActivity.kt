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
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmRingingActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null   // plays alarm sound
    private var vibrator: Vibrator? = null         // handles vibration

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

        // start alarm sound
        startAlarmSound()

        // start vibration
        startVibration()

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
    // play default ringtone as alarm sound
    private fun startAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)  // marks as alarm audio
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmRingingActivity, alarmUri)
                isLooping = true   // keep ringing until dismissed
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // vibrate in pattern - 0ms delay, 500ms on, 500ms off, repeat
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500)  // delay, vibrate, pause pattern
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0)  // 0 = repeat from start
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    // stop alarm sound and release media player
    fun stopAlarmSound() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    // stop vibration
    fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    // stop everything if activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
        stopVibration()
    }
}