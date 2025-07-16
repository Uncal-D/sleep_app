package com.example.sleepapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * 睡眠记录数据模型 - Room本地数据库
 */
@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val date: Date,
    val sleepTime: String,
    val wakeTime: String,
    var sleepOnTime: Boolean = false,
    var wakeOnTime: Boolean = false,
    var usedPhoneDuringSleep: Boolean = false,
    var pointsEarned: Int = 0,
    var streakBonus: Int = 0
)

/**
 * 睡眠记录数据模型 - 与Firebase Firestore同步
 */
data class SleepRecordDto(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val sleepTime: Long = 0,          // 实际睡觉时间
    val wakeTime: Long = 0,           // 实际起床时间
    val points: Int = 0,              // 获得的积分
    val isPhoneUsed: Boolean = false, // 是否在睡眠时间使用手机
    val isAlarmSnoozed: Boolean = false, // 是否贪睡
    val sleepQuality: String = "unknown",
    val isSleepOnTime: Boolean = false,
    val isWakeOnTime: Boolean = false,
    val pointsEarned: Int = 0,
    val streakBonus: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 无参构造函数，Firebase需要
    constructor() : this(
        id = "",
        userId = "",
        date = System.currentTimeMillis(),
        sleepTime = 0,
        wakeTime = 0,
        points = 0,
        isPhoneUsed = false,
        isAlarmSnoozed = false,
        sleepQuality = "unknown",
        isSleepOnTime = false,
        isWakeOnTime = false,
        pointsEarned = 0,
        streakBonus = 0,
        notes = "",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}