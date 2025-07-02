package com.example.sleepapp.data.local.dao

import androidx.room.*
import com.example.sleepapp.data.local.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepRecord(record: SleepRecordEntity)

    @Query("SELECT * FROM sleep_records WHERE id = :recordId")
    fun getSleepRecordById(recordId: Long): Flow<SleepRecordEntity?>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getSleepRecordsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getRecordsInDateRange(userId: String, startDate: Long, endDate: Long): List<SleepRecordEntity>

    @Query("SELECT SUM(points) FROM sleep_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSleepPointsInRange(userId: String, startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(points) FROM sleep_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalWakePointsInRange(userId: String, startDate: Long, endDate: Long): Int?

    // streakBonus 字段数据库没有，先注释掉
    // @Query("SELECT SUM(streakBonus) FROM sleep_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    // suspend fun getTotalBonusPointsInRange(userId: String, startDate: Long, endDate: Long): Int?

    @Update
    suspend fun updateSleepRecord(record: SleepRecordEntity)

    @Delete
    suspend fun deleteSleepRecord(record: SleepRecordEntity)
} 