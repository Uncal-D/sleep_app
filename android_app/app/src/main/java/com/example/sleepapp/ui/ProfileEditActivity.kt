package com.example.sleepapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sleepapp.R
import com.example.sleepapp.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class ProfileEditActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProfileEditActivity", "onCreate started")

        try {
            binding = ActivityProfileEditBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            Log.d("ProfileEditActivity", "Firebase initialized")

            setupToolbar()
            setupClickListeners()
            loadUserData()

            Log.d("ProfileEditActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("ProfileEditActivity", "Error in onCreate", e)
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupToolbar() {
        try {
            // 检查是否已经有ActionBar
            if (supportActionBar == null) {
                setSupportActionBar(binding.toolbar)
            }
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
            Log.d("ProfileEditActivity", "Toolbar setup completed")
        } catch (e: Exception) {
            Log.e("ProfileEditActivity", "Error setting up toolbar", e)
            // 如果Toolbar设置失败，使用简单的返回按钮
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
        }
    }
    
    private fun setupClickListeners() {
        // 简化点击事件，移除头像更换功能
        binding.btnChangeAvatar.setOnClickListener {
            Toast.makeText(this, "头像更换功能暂时不可用", Toast.LENGTH_SHORT).show()
        }

        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }
    
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 设置默认邮箱显示
            val email = currentUser.email ?: ""
            val defaultNickname = if (email.isNotEmpty()) email.substringBefore("@") else "用户"

            try {
                binding.etNickname.setText(defaultNickname)

                // 简化数据加载，避免复杂的异步操作
                firestore.collection("users").document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        try {
                            if (document != null && document.exists()) {
                                val nickname = document.getString("nickname")
                                val phone = document.getString("phone")

                                if (!nickname.isNullOrEmpty()) {
                                    binding.etNickname.setText(nickname)
                                }
                                if (!phone.isNullOrEmpty()) {
                                    binding.etPhone.setText(phone)
                                }
                            }
                        } catch (e: Exception) {
                            // 忽略数据解析错误，使用默认值
                        }
                    }
                    .addOnFailureListener {
                        // 加载失败时使用默认值，不显示错误
                    }
            } catch (e: Exception) {
                // 如果设置默认值也失败，忽略错误
            }
        }
    }
    

    
    private fun saveUserProfile() {
        val currentUser = auth.currentUser ?: return
        
        val nickname = binding.etNickname.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        // 准备用户数据
        val userData = hashMapOf(
            "nickname" to nickname,
            "phone" to phone,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        
        // 直接保存到Firestore，移除头像上传功能
        saveToFirestore(userData)
    }
    
    // 移除头像上传功能，简化代码
    
    private fun saveToFirestore(userData: HashMap<String, Any>) {
        val currentUser = auth.currentUser ?: return

        // 显示保存中状态
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "保存中..."

        // 简化保存逻辑，直接使用set操作
        userData["email"] = currentUser.email ?: ""
        userData["totalPoints"] = 0
        userData["currentStreak"] = 0
        userData["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection("users").document(currentUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                runOnUiThread {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "保存"
                    Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
