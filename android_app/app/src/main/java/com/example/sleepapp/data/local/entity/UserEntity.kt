package com.example.sleepapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val sleepTime: String,
    val wakeTime: String,
    val points: Int,
    val consecutiveDays: Int,
    val createdAt: Long
) 