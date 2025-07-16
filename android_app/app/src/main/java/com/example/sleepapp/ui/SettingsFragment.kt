package com.example.sleepapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.sleepapp.R
import com.example.sleepapp.databinding.FragmentSettingsBinding
import com.example.sleepapp.ui.ProfileEditActivity
import com.example.sleepapp.ui.auth.LoginActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // 加载用户数据
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 设置基本信息
            binding.tvUserName.text = currentUser.email?.substringBefore("@") ?: "用户"
            binding.tvUserEmail.text = currentUser.email ?: ""

            // 从Firestore加载详细信息
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val points = document.getLong("totalPoints") ?: 0
                        binding.tvUserPoints.text = points.toString()

                        // 加载头像
                        val avatarUrl = document.getString("avatar")
                        if (!avatarUrl.isNullOrEmpty() && isAdded && context != null) {
                            try {
                                Glide.with(requireContext())
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.default_avatar)
                                    .error(R.drawable.default_avatar)
                                    .into(binding.ivUserAvatar)
                            } catch (e: Exception) {
                                // 忽略Glide加载错误
                            }
                        }

                        // 更新用户名
                        val nickname = document.getString("nickname")
                        if (!nickname.isNullOrEmpty()) {
                            binding.tvUserName.text = nickname
                        }
                    }
                }
                .addOnFailureListener {
                    binding.tvUserPoints.text = "0"
                }
        }
    }

    private fun setupClickListeners() {
        // 用户信息卡片点击
        binding.cardUserInfo.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileEditActivity::class.java))
        }

        // 个人信息
        binding.layoutPersonalInfo.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileEditActivity::class.java))
        }

        // 修改密码
        binding.layoutChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // 通知设置
        binding.layoutNotifications.setOnClickListener {
            Toast.makeText(context, "通知设置功能开发中", Toast.LENGTH_SHORT).show()
        }

        // 关于我们
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }

        // 退出登录
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("修改密码")
        builder.setMessage("密码修改功能将发送重置链接到您的邮箱，是否继续？")

        builder.setPositiveButton("发送重置邮件") { _, _ ->
            val email = auth.currentUser?.email
            if (!email.isNullOrEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(context, "密码重置邮件已发送，请查收", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "发送失败: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        builder.setNegativeButton("取消", null)
        builder.show()
    }

    private fun showAboutDialog() {
        val aboutMessage = """
            睡眠积分奖励 v1.0.0

            一款帮助您养成良好睡眠习惯的应用
            通过按时睡觉和起床获得积分奖励
            积分可以兑换精美礼品

            开发团队：睡眠健康工作室
            联系邮箱：support@sleepapp.com
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("关于我们")
            .setMessage(aboutMessage)
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定") { _, _ ->
                logout()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}