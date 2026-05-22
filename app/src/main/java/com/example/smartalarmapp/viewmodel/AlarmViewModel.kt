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

    // insert or update alarm in background thread
    fun saveAlarm(alarm: Alarm) = viewModelScope.launch {
        dao.insertAlarm(alarm)
    }

    // delete alarm in background thread
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        dao.deleteAlarm(alarm)
    }

    // toggle alarm on/off without deleting it
    fun toggleAlarm(alarm: Alarm) = viewModelScope.launch {
        dao.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
    }
}