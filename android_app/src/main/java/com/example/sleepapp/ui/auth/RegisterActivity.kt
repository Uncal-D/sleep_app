package com.example.sleepapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sleepapp.R
import com.example.sleepapp.databinding.ActivityRegisterBinding
import com.example.sleepapp.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * 注册界面
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var registerTimeoutRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }
    
    private fun setupListeners() {
        // 注册按钮
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            binding.btnRegister.isEnabled = false
            // 超时保护
            registerTimeoutRunnable?.let { binding.btnRegister.removeCallbacks(it) }
            registerTimeoutRunnable = Runnable {
                runOnUiThread {
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "网络超时，请重试", Toast.LENGTH_SHORT).show()
                }
            }
            binding.btnRegister.postDelayed(registerTimeoutRunnable, 8000)

            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    runOnUiThread {
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.removeCallbacks(registerTimeoutRunnable)
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods != null && signInMethods.isNotEmpty()) {
                                AlertDialog.Builder(this)
                                    .setTitle("账号已注册")
                                    .setMessage("该账号已注册，是否登录？")
                                    .setPositiveButton("是") { _, _ ->
                                        clearInputs()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .setNegativeButton("否") { _, _ ->
                                        clearInputs()
                                    }
                                    .show()
                            } else {
                                registerUser(email, password)
                            }
                        } else {
                            Toast.makeText(this, "网络异常，请重试", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    runOnUiThread {
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.removeCallbacks(registerTimeoutRunnable)
                        Toast.makeText(this, "网络异常，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        
        // 登录按钮
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun registerUser(email: String, password: String) {
        binding.btnRegister.isEnabled = false
        // 超时保护
        registerTimeoutRunnable?.let { binding.btnRegister.removeCallbacks(it) }
        registerTimeoutRunnable = Runnable {
            runOnUiThread {
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, "网络超时，请重试", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnRegister.postDelayed(registerTimeoutRunnable, 8000)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                runOnUiThread {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.removeCallbacks(registerTimeoutRunnable)
                    if (task.isSuccessful) {
                        AlertDialog.Builder(this)
                            .setTitle("注册成功")
                            .setMessage("是否现在登录？")
                            .setPositiveButton("是") { _, _ ->
                                clearInputs()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .setNegativeButton("否") { _, _ ->
                                clearInputs()
                            }
                            .show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.error_register),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener {
                runOnUiThread {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.removeCallbacks(registerTimeoutRunnable)
                    Toast.makeText(this, "网络异常，请重试", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearInputs() {
        binding.etEmail.setText("")
        binding.etPassword.setText("")
        binding.etConfirmPassword.setText("")
    }
} 