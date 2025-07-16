package com.example.sleepapp.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatTime(millis: Long, pattern: String = "HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun parseTime(time: String, pattern: String = "HH:mm"): Long {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.parse(time)?.time ?: 0L
    }

    fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
} 