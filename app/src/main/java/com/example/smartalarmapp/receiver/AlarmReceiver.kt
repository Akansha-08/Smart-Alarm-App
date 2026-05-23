package com.example.smartalarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartalarmapp.ui.AlarmRingingActivity

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

        context.startActivity(ringingIntent)
    }
}