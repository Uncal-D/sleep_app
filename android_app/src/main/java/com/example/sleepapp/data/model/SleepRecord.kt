package com.example.sleepapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * 睡眠记录数据模型
 */
@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val date: Date,
    val sleepTime: String,
    val wakeTime: String,
    var sleepOnTime: Boolean = false,     // 是否按时睡觉
    var wakeOnTime: Boolean = false,      // 是否按时起床
    var usedPhoneDuringSleep: Boolean = false, // 睡眠期间是否使用手机
    var pointsEarned: Int = 0,            // 获得的积分
    var streakBonus: Int = 0              // 连续奖励积分
) 

data class SleepRecordDto(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val sleepTime: Long = 0,          // 实际睡觉时间
    val wakeTime: Long = 0,           // 实际起床时间
    val points: Int = 0,              // 获得的积分
    val isPhoneUsed: Boolean = false, // 是否在睡眠时间使用手机
    val isAlarmSnoozed: Boolean = false // 是否贪睡
) 