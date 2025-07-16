package com.example.sleepapp.data.model

import java.util.*

/**
 * 用户数据模型 - 简化版本，与Firebase Firestore同步
 */
data class User(
    val id: String = "",
    val email: String = "",
    val nickname: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val sleepTime: String = "23:00",
    val wakeTime: String = "07:00",
    var totalPoints: Int = 0,
    var currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val status: String = "active",
    val sleepStatus: String = "unknown",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = 0,
    val timezone: String = "Asia/Shanghai",
    // 兼容旧版本字段
    val userId: String = "",
    var sleepPointsToday: Int = 0,
    var wakePointsToday: Int = 0,
    var bonusPointsToday: Int = 0
) {
    // 无参构造函数，Firebase需要
    constructor() : this(
        id = "",
        email = "",
        nickname = "",
        phone = "",
        avatarUrl = "",
        sleepTime = "23:00",
        wakeTime = "07:00",
        totalPoints = 0,
        currentStreak = 0,
        maxStreak = 0,
        status = "active",
        sleepStatus = "unknown",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        lastLoginAt = 0,
        timezone = "Asia/Shanghai",
        userId = "",
        sleepPointsToday = 0,
        wakePointsToday = 0,
        bonusPointsToday = 0
    )
}