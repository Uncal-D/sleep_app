package com.example.sleepapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sleepapp.data.local.dao.SleepRecordDao
import com.example.sleepapp.data.local.dao.UserDao
import com.example.sleepapp.data.local.entity.SleepRecordEntity
import com.example.sleepapp.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, SleepRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sleepRecordDao(): SleepRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sleep_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 