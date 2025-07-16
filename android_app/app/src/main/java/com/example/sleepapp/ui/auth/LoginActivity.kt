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
import kotlinx.coroutines.tasks.await

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
                        when {
                            exception is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                Toast.makeText(
                                    this,
                                    "该账号不存在，请先创建账号",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            exception?.message?.contains("There is no user record") == true -> {
                                Toast.makeText(
                                    this,
                                    "该账号不存在，请先创建账号",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            exception?.message?.contains("user-not-found") == true -> {
                                Toast.makeText(
                                    this,
                                    "该账号不存在，请先创建账号",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(
                                    this,
                                    "密码错误，请重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            exception?.message?.contains("password is invalid") == true -> {
                                Toast.makeText(
                                    this,
                                    "密码错误，请重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            exception?.message?.contains("The user account has been disabled") == true -> {
                                Toast.makeText(
                                    this,
                                    "账号凭证已失效，请重新登录或重置密码",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            exception != null && (exception.localizedMessage?.contains("network") == true || exception.localizedMessage?.contains("Network") == true) -> {
                                Toast.makeText(
                                    this,
                                    "网络异常，请重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            exception != null -> {
                                Toast.makeText(
                                    this,
                                    "登录失败，请检查网络或稍后重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this,
                                    "登录失败，请检查网络或稍后重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
    }
    
    private fun checkUserSetup() {
        val userId = auth.currentUser?.uid ?: return

        // 使用协程在后台线程执行Firebase操作
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val document = firestore.collection("users").document(userId)
                    .get()
                    .await()

                // 切换到主线程执行UI操作
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (document != null && document.exists()) {
                        // 用户数据存在，直接进入主界面
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        // 用户数据不存在，进入引导页
                        startActivity(Intent(this@LoginActivity, OnboardingActivity::class.java))
                    }
                    finish()
                }
            } catch (e: Exception) {
                // 切换到主线程处理错误
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    // 获取用户数据失败，进入引导页
                    startActivity(Intent(this@LoginActivity, OnboardingActivity::class.java))
                    finish()
                }
            }
        }
    }
} 