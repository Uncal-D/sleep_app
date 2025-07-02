package com.example.sleepapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sleepapp.R
import com.example.sleepapp.databinding.ActivityLoginBinding
import com.example.sleepapp.ui.MainActivity
import com.example.sleepapp.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * 登录界面
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var loginTimeoutRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        setupListeners()
    }
    
    private fun setupListeners() {
        // 登录按钮
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写邮箱和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            loginUser(email, password)
        }
        
        // 注册按钮
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        // 超时保护
        loginTimeoutRunnable?.let { binding.btnLogin.removeCallbacks(it) }
        loginTimeoutRunnable = Runnable {
            runOnUiThread {
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "网络超时，请重试", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnLogin.postDelayed(loginTimeoutRunnable, 8000)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.removeCallbacks(loginTimeoutRunnable)
                    if (task.isSuccessful) {
                        // 登录成功，检查用户是否已设置睡眠时间
                        checkUserSetup()
                    } else {
                        // 登录失败，判断错误类型
                        val exception = task.exception
                        if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException || exception?.message?.contains("There is no user record corresponding to this identifier") == true) {
                            Toast.makeText(
                                this,
                                "账号不存在，请先注册",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (exception != null) {
                            Toast.makeText(
                                this,
                                exception.localizedMessage ?: getString(R.string.error_login),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.error_login),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.removeCallbacks(loginTimeoutRunnable)
                    Toast.makeText(this, "网络异常，请重试", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun checkUserSetup() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // 用户数据存在，直接进入主界面
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // 用户数据不存在，进入引导页
                    startActivity(Intent(this, OnboardingActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                // 获取用户数据失败，进入引导页
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            }
    }
} 