package com.example.smartalarmapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")           // tells Room this is a DB table called "alarms"
data class Alarm(
    @PrimaryKey(autoGenerate = true)    // auto-increments ID for each new alarm
    val id: Int = 0,
    val hour: Int,                      // 0-23 format
    val minute: Int,                    // 0-59
    val label: String = "",             // optional alarm name e.g. "Wake up"
    val isEnabled: Boolean = true,      // toggle on/off without deleting
    val repeatDays: String = "",        // comma-separated e.g. "Mon,Wed,Fri"
    val stepCount: Int = 10             // steps needed to dismiss the alarm
)