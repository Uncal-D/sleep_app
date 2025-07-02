package com.example.sleepapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sleepapp.data.repository.SleepRepository
import com.example.sleepapp.service.AlarmReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * 闹钟帮助类
 */
class AlarmHelper(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * 设置睡眠闹钟
     * @param sleepTime 睡觉时间 (HH:mm)
     */
    fun setSleepAlarm(sleepTime: String) {
        val sleepTimeMillis = AlarmHelper.getTodaySleepTimeMillis(sleepTime)
        
        // 如果时间已过，设置为明天
        val now = System.currentTimeMillis()
        val triggerTime = if (sleepTimeMillis <= now) {
            sleepTimeMillis + 24 * 60 * 60 * 1000
        } else {
            sleepTimeMillis
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmHelper.EXTRA_ALARM_TYPE, AlarmHelper.ALARM_TYPE_SLEEP)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * 设置起床闹钟
     * @param wakeTime 起床时间 (HH:mm)
     */
    fun setWakeAlarm(wakeTime: String) {
        val wakeTimeMillis = AlarmHelper.getTodayWakeTimeMillis(wakeTime)
        
        // 如果时间已过，设置为明天
        val now = System.currentTimeMillis()
        val triggerTime = if (wakeTimeMillis <= now) {
            wakeTimeMillis + 24 * 60 * 60 * 1000
        } else {
            wakeTimeMillis
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmHelper.EXTRA_ALARM_TYPE, AlarmHelper.ALARM_TYPE_WAKE)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * 取消闹钟
     */
    fun cancelAlarms() {
        val sleepIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmHelper.EXTRA_ALARM_TYPE, AlarmHelper.ALARM_TYPE_SLEEP)
        }
        
        val sleepPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            sleepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val wakeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmHelper.EXTRA_ALARM_TYPE, AlarmHelper.ALARM_TYPE_WAKE)
        }
        
        val wakePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            wakeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(sleepPendingIntent)
        alarmManager.cancel(wakePendingIntent)
    }
    
    /**
     * 从用户数据中设置闹钟
     */
    fun scheduleAlarms() {
        val currentUser = auth.currentUser ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val sleepTime = userDoc.getString("sleepTime") ?: "23:00"
                val wakeTime = userDoc.getString("wakeTime") ?: "07:00"
                
                setSleepAlarm(sleepTime)
                setWakeAlarm(wakeTime)
            } catch (e: Exception) {
                // 获取用户数据失败，使用默认时间
                setSleepAlarm("23:00")
                setWakeAlarm("07:00")
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val ALARM_TYPE_SLEEP = "sleep"
        const val ALARM_TYPE_WAKE = "wake"
        fun getTodaySleepTimeMillis(sleepTime: String): Long {
            val calendar = java.util.Calendar.getInstance()
            val parts = sleepTime.split(":")
            calendar.set(java.util.Calendar.HOUR_OF_DAY, parts[0].toInt())
            calendar.set(java.util.Calendar.MINUTE, parts[1].toInt())
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
        fun getTodayWakeTimeMillis(wakeTime: String): Long {
            val calendar = java.util.Calendar.getInstance()
            val parts = wakeTime.split(":")
            calendar.set(java.util.Calendar.HOUR_OF_DAY, parts[0].toInt())
            calendar.set(java.util.Calendar.MINUTE, parts[1].toInt())
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }
} 