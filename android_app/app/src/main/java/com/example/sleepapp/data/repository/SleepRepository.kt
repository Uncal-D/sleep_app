package com.example.sleepapp.data.repository

import com.example.sleepapp.data.model.SleepRecordDto
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    suspend fun addSleepRecord(record: SleepRecordDto): Result<SleepRecordDto>
    suspend fun getSleepRecord(recordId: String): Result<SleepRecordDto>
    fun getSleepRecordsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<SleepRecordDto>>
    suspend fun updateSleepRecord(record: SleepRecordDto): Result<SleepRecordDto>
    suspend fun deleteSleepRecord(recordId: String): Result<Unit>
} 