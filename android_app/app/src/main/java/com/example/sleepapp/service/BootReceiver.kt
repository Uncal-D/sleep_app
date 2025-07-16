package com.example.sleepapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sleepapp.utils.AlarmHelper

/**
 * 开机启动广播接收器
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 启动睡眠监控服务
            val serviceIntent = Intent(context, SleepMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            // 重新设置闹钟
            val alarmHelper = AlarmHelper(context)
            alarmHelper.scheduleAlarms()
        }
    }
}
 