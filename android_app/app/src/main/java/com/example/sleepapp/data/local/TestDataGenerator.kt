package com.example.sleepapp.data.local

import com.example.sleepapp.data.local.entity.SleepRecordEntity
import com.example.sleepapp.data.local.entity.UserEntity
import java.util.*

object TestDataGenerator {
    fun generateTestUsers(): List<UserEntity> {
        return listOf(
            UserEntity(
                id = "user1",
                email = "test1@example.com",
                sleepTime = "23:00",
                wakeTime = "07:00",
                points = 100,
                consecutiveDays = 5,
                createdAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = "user2",
                email = "test2@example.com",
                sleepTime = "22:30",
                wakeTime = "06:30",
                points = 150,
                consecutiveDays = 7,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun generateTestSleepRecords(userId: String): List<SleepRecordEntity> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7) // 从7天前开始
        
        return (0..6).map { dayOffset ->
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val date = calendar.timeInMillis
            
            SleepRecordEntity(
                userId = userId,
                date = date,
                sleepTime = date - 8 * 60 * 60 * 1000, // 8小时前
                wakeTime = date,
                points = if (dayOffset % 2 == 0) 10 else 5,
                isPhoneUsed = dayOffset % 3 == 0,
                isAlarmSnoozed = dayOffset % 4 == 0
            )
        }
    }
} 