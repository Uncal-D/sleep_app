package com.example.sleepapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SleepMonitorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
} 