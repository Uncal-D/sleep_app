package com.example.sleepapp.data.model

import java.util.*

/**
 * 用户数据模型
 */
data class User(
    val id: String = "",
    val email: String = "",
    val sleepTime: String = "23:00", // 默认睡觉时间
    val wakeTime: String = "07:00",  // 默认起床时间
    val points: Int = 0,             // 用户积分
    val consecutiveDays: Int = 0,    // 连续达标天数
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "",
    var totalPoints: Int = 0,
    var currentStreak: Int = 0,
    var sleepPointsToday: Int = 0,
    var wakePointsToday: Int = 0,
    var bonusPointsToday: Int = 0
) 