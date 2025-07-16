package com.example.sleepapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepapp.data.model.SleepRecordDto
import com.example.sleepapp.data.repository.SleepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SleepViewModel(
    private val sleepRepository: SleepRepository
) : ViewModel() {
    private val _sleepRecords = MutableStateFlow<List<SleepRecordDto>>(emptyList())
    val sleepRecords: StateFlow<List<SleepRecordDto>> = _sleepRecords

    fun addSleepRecord(record: SleepRecordDto) {
        viewModelScope.launch {
            sleepRepository.addSleepRecord(record).onSuccess { newRecord ->
                _sleepRecords.value = _sleepRecords.value + newRecord
            }
        }
    }

    fun loadSleepRecords(userId: String, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            sleepRepository.getSleepRecordsByDateRange(userId, startDate, endDate)
                .collect { records ->
                    _sleepRecords.value = records
                }
        }
    }

    fun updateSleepRecord(record: SleepRecordDto) {
        viewModelScope.launch {
            sleepRepository.updateSleepRecord(record).onSuccess { updatedRecord ->
                _sleepRecords.value = _sleepRecords.value.map { 
                    if (it.id == updatedRecord.id) updatedRecord else it 
                }
            }
        }
    }
} 