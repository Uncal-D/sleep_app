package com.example.sleepapp.ui.onboarding

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sleepapp.R
import com.example.sleepapp.data.model.User
import com.example.sleepapp.databinding.ActivityOnboardingBinding
import com.example.sleepapp.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * 引导页
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    private var sleepTime = "23:00"
    private var wakeTime = "07:00"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        setupListeners()
        updateTimeDisplay()
    }
    
    private fun setupListeners() {
        // 睡觉时间选择
        binding.btnSleepTime.setOnClickListener {
            showTimePickerDialog(true)
        }
        
        // 起床时间选择
        binding.btnWakeTime.setOnClickListener {
            showTimePickerDialog(false)
        }
        
        // 开始使用按钮
        binding.btnStart.setOnClickListener {
            saveUserSettings()
        }
    }
    
    /**
     * 显示时间选择对话框
     */
    private fun showTimePickerDialog(isSleepTime: Boolean) {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val time = if (isSleepTime) sleepTime else wakeTime
        try {
            val date = timeFormat.parse(time)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // 使用当前时间
        }
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (isSleepTime) {
                    sleepTime = selectedTime
                } else {
                    wakeTime = selectedTime
                }
                updateTimeDisplay()
            },
            hour,
            minute,
            true
        )
        
        timePickerDialog.show()
    }
    
    /**
     * 更新时间显示
     */
    private fun updateTimeDisplay() {
        binding.tvSleepTime.text = getString(R.string.sleep_time) + ": " + sleepTime
        binding.tvWakeTime.text = getString(R.string.wake_time) + ": " + wakeTime
    }
    
    /**
     * 保存用户设置
     */
    private fun saveUserSettings() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnStart.isEnabled = false
        
        val user = User(
            userId = userId,
            email = auth.currentUser?.email ?: "",
            sleepTime = sleepTime,
            wakeTime = wakeTime
        )
        
        firestore.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                // 进入主界面
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                binding.btnStart.isEnabled = true
                Toast.makeText(this, "保存设置失败，请重试", Toast.LENGTH_SHORT).show()
            }
    }
} 