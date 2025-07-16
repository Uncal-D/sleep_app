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
import android.util.Log

/**
 * 应用程序类
 */
class SleepApp : Application(), Configuration.Provider {

    // 实现WorkManager配置属性
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("CRASH", "=== 应用崩溃了！===")
            Log.e("CRASH", "线程: ${thread.name}")
            Log.e("CRASH", "异常类型: ${exception.javaClass.simpleName}")
            Log.e("CRASH", "异常消息: ${exception.message}")
            Log.e("CRASH", "堆栈跟踪:")
            exception.printStackTrace()

            // 调用系统默认处理器
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandler?.uncaughtException(thread, exception)
        }

        // 设置ANR监控
        setupANRWatchdog()

        // 初始化Firebase
        FirebaseApp.initializeApp(this)

        // 设置每日检查任务
        setupDailyCheckWorker()

        // 在后台线程初始化数据库，避免阻塞主线程
        Thread {
            AppDatabase.getDatabase(this)
        }.start()
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

    /**
     * 设置ANR监控
     */
    private fun setupANRWatchdog() {
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

        // 每5秒检查一次主线程是否响应
        val watchdogRunnable = object : Runnable {
            override fun run() {
                val startTime = System.currentTimeMillis()

                mainHandler.post {
                    val endTime = System.currentTimeMillis()
                    val delay = endTime - startTime

                    if (delay > 5000) { // 如果延迟超过5秒，可能发生ANR
                        Log.w("ANR_WATCHDOG", "检测到可能的ANR，主线程延迟: ${delay}ms")
                    }
                }

                // 继续监控
                mainHandler.postDelayed(this, 5000)
            }
        }

        // 在后台线程启动监控
        Thread {
            Thread.sleep(10000) // 等待应用启动完成
            mainHandler.post(watchdogRunnable)
        }.start()
    }
}