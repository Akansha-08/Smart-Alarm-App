package com.example.smartalarmapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao                                                    // Data Access Object - defines all DB operations
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>>               // Flow = auto-updates UI on DB change

    @Insert(onConflict = OnConflictStrategy.REPLACE)    // replaces if same ID exists
    suspend fun insertAlarm(alarm: Alarm)               // suspend = runs in background thread

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmAndGetId(alarm: Alarm): Long  // returns auto-generated ID after insert

    @Update
    suspend fun updateAlarm(alarm: Alarm)               // updates existing alarm by ID

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)               // deletes by matching ID

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?           // ? means can return null if not found
}