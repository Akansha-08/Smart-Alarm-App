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
        dao.insertAlarm(alarm)
        // only schedule if alarm is enabled
        if (alarm.isEnabled) {
            AlarmScheduler.scheduleAlarm(getApplication(), alarm)
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