package com.example.smartalarmapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartalarmapp.data.Alarm
import com.example.smartalarmapp.data.AlarmDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.smartalarmapp.utils.AlarmScheduler

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    // get DAO from database singleton
    private val dao = AlarmDatabase.getDatabase(application).alarmDao()

    // convert Flow to StateFlow so Compose UI can observe it
    val alarms: StateFlow<List<Alarm>> = dao.getAllAlarms()
        .stateIn(
            scope = viewModelScope,          // tied to ViewModel lifecycle
            started = SharingStarted.WhileSubscribed(5000), // keep alive 5s after UI gone
            initialValue = emptyList()       // show empty list before DB loads
        )

    // insert or update alarm and schedule it
    fun saveAlarm(alarm: Alarm) = viewModelScope.launch {
        if (alarm.id == 0) {
            // new alarm - get auto-generated ID from Room after insert
            val newId = dao.insertAlarmAndGetId(alarm)
            // create copy with real ID so scheduler uses correct ID
            val savedAlarm = alarm.copy(id = newId.toInt())
            if (savedAlarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), savedAlarm)
            }
        } else {
            // existing alarm being edited - cancel old schedule first
            AlarmScheduler.cancelAlarm(getApplication(), alarm)
            dao.insertAlarm(alarm)
            if (alarm.isEnabled) {
                // reschedule with same ID
                AlarmScheduler.scheduleAlarm(getApplication(), alarm)
            }
        }
    }

    // delete alarm and cancel its schedule
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        dao.deleteAlarm(alarm)
        AlarmScheduler.cancelAlarm(getApplication(), alarm)  // cancel from AlarmManager
    }

    // toggle alarm on/off and update schedule accordingly
    fun toggleAlarm(alarm: Alarm) = viewModelScope.launch {
        val updated = alarm.copy(isEnabled = !alarm.isEnabled)
        dao.updateAlarm(updated)
        if (updated.isEnabled) {
            AlarmScheduler.scheduleAlarm(getApplication(), updated)  // re-schedule
        } else {
            AlarmScheduler.cancelAlarm(getApplication(), updated)    // cancel
        }
    }
}