package com.example.sleepapp.data.repository

import com.example.sleepapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUser(user: User): Result<User>
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<User>
    fun observeUser(userId: String): Flow<User>
    suspend fun deleteUser(userId: String): Result<Unit>
} 