package com.example.smartalarmapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.media.Ringtone

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val ringtone: Ringtone = RingtoneManager.getRingtone(context, uri)

        ringtone.play()
    }
}