package com.example.sleepapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepapp.data.model.User
import com.example.sleepapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun createUser(email: String) {
        viewModelScope.launch {
            val newUser = User(email = email)
            userRepository.createUser(newUser).onSuccess { user ->
                _user.value = user
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user).onSuccess { updatedUser ->
                _user.value = updatedUser
            }
        }
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            userRepository.getUser(userId).onSuccess { user ->
                _user.value = user
            }
        }
    }
} 