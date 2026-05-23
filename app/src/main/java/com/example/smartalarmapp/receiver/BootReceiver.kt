package com.example.smartalarmapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartalarmapp.data.AlarmDatabase
import com.example.smartalarmapp.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    // called when phone reboots - AlarmManager clears all alarms on reboot
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            // re-schedule all enabled alarms from DB after reboot
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AlarmDatabase.getDatabase(context).alarmDao()
                val alarms = dao.getAllAlarms().first()  // get current list once

                alarms.filter { it.isEnabled }          // only reschedule enabled alarms
                    .forEach { alarm ->
                        AlarmScheduler.scheduleAlarm(context, alarm)
                    }
            }
        }
    }
}