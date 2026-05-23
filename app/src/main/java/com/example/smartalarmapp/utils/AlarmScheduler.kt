package com.example.smartalarmapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.smartalarmapp.data.Alarm
import com.example.smartalarmapp.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    // schedule alarm using AlarmManager
    fun scheduleAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // intent that fires AlarmReceiver when alarm triggers
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)        // pass alarm id to receiver
            putExtra("ALARM_LABEL", alarm.label)  // pass label to show in notification
            putExtra("STEP_COUNT", alarm.stepCount) // pass required steps
        }

        // pending intent wraps the intent - needed by AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,                             // unique request code per alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // calculate next trigger time
        val triggerTime = getNextTriggerTime(alarm)

        // setExactAndAllowWhileIdle fires even in doze/battery saver mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,  // wake up device when alarm fires
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    // cancel a scheduled alarm
    fun cancelAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // must match exact same intent used in scheduleAlarm
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)  // cancels the alarm
    }

    // calculate next trigger time based on alarm settings
    private fun getNextTriggerTime(alarm: Alarm): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // if time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return calendar.timeInMillis
    }
}