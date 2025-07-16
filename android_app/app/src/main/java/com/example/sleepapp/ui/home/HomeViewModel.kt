package com.example.sleepapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepapp.data.local.AppDatabase
import com.example.sleepapp.data.model.SleepRecord
import com.example.sleepapp.data.model.SleepRecordDto
import com.example.sleepapp.data.model.User
import com.example.sleepapp.data.repository.SleepRepositoryImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 主页ViewModel
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sleepRecordDao = AppDatabase.getDatabase(application).sleepRecordDao()
    private val repository = SleepRepositoryImpl()
    
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData
    
    private val _todaySleepRecord = MutableLiveData<SleepRecordDto>()
    val todaySleepRecord: LiveData<SleepRecordDto> = _todaySleepRecord
    
    init {
        loadUserData()
        loadTodaySleepRecord()
    }
    
    /**
     * 加载用户数据
     */
    fun loadUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUserData()
                withContext(Dispatchers.Main) {
                    _userData.postValue(user)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userData.postValue(null)
                }
            }
        }
    }
    
    /**
     * 加载今天的睡眠记录
     */
    fun loadTodaySleepRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = Date()
                val record = repository.getSleepRecordForDate(today)
                withContext(Dispatchers.Main) {
                    _todaySleepRecord.postValue(record)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _todaySleepRecord.postValue(null)
                }
            }
        }
    }
} 