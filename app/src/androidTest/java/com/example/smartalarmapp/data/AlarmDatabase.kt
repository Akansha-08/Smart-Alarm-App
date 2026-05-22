package com.example.smartalarmapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alarm::class], version = 1, exportSchema = false)
// entities = list of tables, version = schema version for migrations

abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao                           // entry point to DB operations

    companion object {
        @Volatile  // changes to this variable are immediately visible to all threads
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            // singleton pattern - only one DB instance ever created
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,                 // use app context to avoid memory leaks
                    AlarmDatabase::class.java,
                    "alarm_database"                    // name of the .db file on device
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}