package com.example.smartalarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartalarmapp.ui.AlarmRingingActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    // called by system when alarm time hits
    override fun onReceive(context: Context, intent: Intent) {

        // extract alarm details passed from AlarmScheduler
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val stepCount = intent.getIntExtra("STEP_COUNT", 10)

        // start ringing activity - shows fullscreen alarm UI
        val ringingIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("STEP_COUNT", stepCount)
            // these flags open activity from background/receiver context
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        // full screen pending intent - works even when app is in background
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            ringingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // create notification channel first (required for Android 8+)
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH  // high importance = shows as heads up
            ).apply {
                description = "Smart Alarm notifications"
                enableVibration(true)                  // vibrate with notification
                setBypassDnd(true)                     // bypass Do Not Disturb
                lockscreenVisibility =
                    NotificationCompat.VISIBILITY_PUBLIC  // show on lock screen
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // build notification with fullScreenIntent
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarmLabel)
            .setContentText("Walk $stepCount steps to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)         // MAX not HIGH
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)                                      // can't be swiped away
            .setAutoCancel(false)                                  // stays until dismissed
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // shows on lock screen
            .build()

        // show notification - this triggers fullscreen intent
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId, notification)

    }
}