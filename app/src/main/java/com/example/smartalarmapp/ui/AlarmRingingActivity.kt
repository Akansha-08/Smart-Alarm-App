package com.example.smartalarmapp.ui

import android.os.Bundle
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import com.example.smartalarmapp.ui.theme.SmartAlarmAppTheme
import com.example.smartalarmapp.data.AlarmDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.smartalarmapp.utils.StepDetector
import androidx.compose.runtime.mutableStateOf


class AlarmRingingActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null   // plays alarm sound
    private var vibrator: Vibrator? = null         // handles vibration

    private var stepDetector: StepDetector? = null  // handles step counting

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

        // check if alarm was disabled before it could be canceled
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

        // state to track current steps in UI
        val currentStepsState = mutableStateOf(0)
        // state to show warning when invalid step detected
        val invalidStepState = mutableStateOf(false)

        // initialize step detector
        stepDetector = StepDetector(
            context = this,
            requiredSteps = stepCount,
            onStepCountChanged = { steps ->
                currentStepsState.value = steps  // update UI with new step count
                invalidStepState.value = false  // clear warning on valid step
            },
            onGoalReached = {
                // goal reached - dismiss alarm
                runOnUiThread {
                    stopAlarmSound()
                    stopVibration()
                    stepDetector?.stop()
                    finish()
                }
            },
            onInvalidStep = {
                // show warning on UI thread
                runOnUiThread {
                    invalidStepState.value = true  // trigger warning message
                }
            }
        )
        stepDetector?.start()

        setContent {
            SmartAlarmAppTheme {
                AlarmRingingScreen(
                    alarmLabel = alarmLabel,
                    stepCount = stepCount,
                    currentSteps = currentStepsState.value,
                    invalidStep = invalidStepState.value,  // pass warning state
                    onDismiss = {
                        stopAlarmSound()
                        stopVibration()
                        stepDetector?.stop()
                        finish()
                    }
                )
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
        stepDetector?.stop()  // unregister sensor listener
    }
}
@Composable
fun AlarmRingingScreen(
    alarmLabel: String,
    stepCount: Int,
    currentSteps: Int = 0,   // live step count from sensor
    invalidStep: Boolean = false,  // true when invalid step detected
    onDismiss: () -> Unit
) {
    // pulsing animation for the alarm circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),  // 800ms pulse cycle
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // progress from 0.0 to 1.0 based on steps walked
    val progress = (currentSteps.toFloat() / stepCount.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer), // red-ish background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // pulsing alarm circle
            Box(
                modifier = Modifier
                    .scale(scale)                    // apply pulse animation
                    .size(150.dp)
                    .background(
                        MaterialTheme.colorScheme.error,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("⏰", fontSize = 60.sp)
            }

            // alarm label
            Text(
                text = alarmLabel,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            // live step counter display
            Text(
                text = "$currentSteps / $stepCount steps",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            // progress bar showing steps walked
            LinearProgressIndicator(
                progress = { progress },             // 0.0 to 1.0
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
            )

            // instruction text
            Text(
                text = if (currentSteps == 0) "Start walking to dismiss!"
                else if (progress < 1f) "Keep walking... ${stepCount - currentSteps} steps left"
                else "Done! Dismissing...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            // hint for best step detection
            Text(
                text = "💡 Hold phone naturally or keep in pocket",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.5f)
            )

            // anti-cheat warning - shows when tapping/shaking detected
            if (invalidStep) {
                Text(
                    text = "⚠️ Walk naturally — tapping detected!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}