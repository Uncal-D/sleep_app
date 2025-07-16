package com.example.sleepapp.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepapp.data.local.AppDatabase
import com.example.sleepapp.data.repository.SleepRepositoryImpl
import kotlinx.coroutines.launch
import java.util.*

/**
 * 统计数据模型
 */
data class StatsData(
    val totalDays: Int = 0,
    val successDays: Int = 0,
    val sleepPoints: Int = 0,
    val wakePoints: Int = 0,
    val bonusPoints: Int = 0,
    val totalPoints: Int = 0,
    val maxStreak: Int = 0
)

/**
 * 统计页面ViewModel
 */
class StatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sleepRecordDao = AppDatabase.getDatabase(application).sleepRecordDao()
    private val repository = SleepRepositoryImpl()
    
    private val _statsData = MutableLiveData<StatsData>()
    val statsData: LiveData<StatsData> = _statsData
    
    /**
     * 加载统计数据
     */
    fun loadStatsData(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            // 获取用户ID
            val user = repository.getUserData()
            val userId = user.userId
            
            // 获取指定日期范围内的所有记录
            val records = sleepRecordDao.getRecordsInDateRange(userId, startDate.time, endDate.time)
            
            // 计算总天数
            val totalDays = records.size
            
            // 计算成功天数（睡前和起床都成功且没有夜间使用手机）
            val successDays = records.count { 
                !it.isAlarmSnoozed && !it.isPhoneUsed // 假设isAlarmSnoozed为贪睡，isPhoneUsed为夜间用手机
            }
            
            // 计算积分
            val sleepPoints = sleepRecordDao.getTotalSleepPointsInRange(userId, startDate.time, endDate.time) ?: 0
            val wakePoints = sleepRecordDao.getTotalWakePointsInRange(userId, startDate.time, endDate.time) ?: 0
            // val bonusPoints = sleepRecordDao.getTotalBonusPointsInRange(userId, startDate, endDate) ?: 0 // 已注释
            val bonusPoints = 0 // 目前数据库没有奖励积分字段
            val totalPoints = sleepPoints + wakePoints + bonusPoints
            
            // 计算最长连续天数
            var currentStreak = 0
            var maxStreak = 0
            
            // 按日期排序
            val sortedRecords = records.sortedBy { it.date }
            
            for (record in sortedRecords) {
                if (!record.isAlarmSnoozed && !record.isPhoneUsed) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 0
                }
            }
            
            // 更新统计数据
            _statsData.postValue(
                StatsData(
                    totalDays = totalDays,
                    successDays = successDays,
                    sleepPoints = sleepPoints,
                    wakePoints = wakePoints,
                    bonusPoints = bonusPoints,
                    totalPoints = totalPoints,
                    maxStreak = maxStreak
                )
            )
        }
    }
} 