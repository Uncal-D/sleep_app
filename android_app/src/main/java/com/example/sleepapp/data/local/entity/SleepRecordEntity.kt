package com.example.sleepapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val date: Long,
    val sleepTime: Long,
    val wakeTime: Long,
    val points: Int,
    val isPhoneUsed: Boolean,
    val isAlarmSnoozed: Boolean
) 