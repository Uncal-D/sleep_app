package com.example.sleepapp

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sleepapp.service.DailyCheckWorker
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit
import com.example.sleepapp.data.local.AppDatabase

/**
 * 应用程序类
 */
class SleepApp : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Firebase
        FirebaseApp.initializeApp(this)
        
        // 设置每日检查任务
        setupDailyCheckWorker()
        
        // 初始化数据库
        AppDatabase.getDatabase(this)
    }
    
    /**
     * 设置每日检查任务
     */
    private fun setupDailyCheckWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val dailyCheckRequest = PeriodicWorkRequestBuilder<DailyCheckWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_check",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyCheckRequest
        )
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
} 