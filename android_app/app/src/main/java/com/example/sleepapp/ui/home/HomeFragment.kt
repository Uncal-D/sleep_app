package com.example.sleepapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sleepapp.R
import com.example.sleepapp.databinding.FragmentHomeBinding
import com.example.sleepapp.utils.TimeUtils
import com.example.sleepapp.utils.AlarmHelper
import com.example.sleepapp.utils.AlarmHelper.Companion.getTodaySleepTimeMillis
import com.example.sleepapp.utils.AlarmHelper.Companion.getTodayWakeTimeMillis
import com.example.sleepapp.utils.AlarmHelper.Companion.EXTRA_ALARM_TYPE
import com.example.sleepapp.utils.AlarmHelper.Companion.ALARM_TYPE_SLEEP
import com.example.sleepapp.utils.AlarmHelper.Companion.ALARM_TYPE_WAKE
import java.util.*
import java.text.SimpleDateFormat
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog

/**
 * 主页Fragment
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupUI()
        setupObservers()
        startCountdownTimer()

        // 只在首次启动时显示欢迎弹窗
        if (com.example.sleepapp.ui.MainActivity.isFirstLaunch) {
            showWelcomeDialog()
            com.example.sleepapp.ui.MainActivity.isFirstLaunch = false
        }
    }

    private fun setupUI() {
        // 设置当前日期
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
        val currentDate = dateFormat.format(Date())
        binding.tvDate.text = currentDate

        // 设置默认值
        try {
            binding.tvWelcome.text = "欢迎使用睡眠积分奖励"
            binding.tvPoints.text = "0"
            binding.tvSleepTime.text = "22:10"
            binding.tvWakeTime.text = "07:10"
            binding.tvCountdown.text = "距离下次睡觉还有：03:19:22"
            binding.tvPoetry.text = "春眠不觉晓，处处闻啼鸟。"
            binding.tvPoetryAuthor.text = "—— 孟浩然《春晓》"
            binding.tvSleepStatus.text = "达标"
        } catch (e: Exception) {
            // 忽略视图绑定错误
        }
    }

    private fun setupObservers() {
        // 观察用户数据
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                // 更新欢迎信息
                val userEmail = it.email
                val welcomeText = if (userEmail.isNotEmpty()) {
                    "欢迎回来，${userEmail.substringBefore("@")}"
                } else {
                    "欢迎使用睡眠积分奖励"
                }
                binding.tvWelcome.text = welcomeText

                binding.tvPoints.text = it.totalPoints.toString()

                // 更新睡眠时间显示
                binding.tvSleepTime.text = it.sleepTime
                binding.tvWakeTime.text = it.wakeTime

                // 更新睡眠状态
                binding.tvSleepStatus.text = when(it.sleepStatus) {
                    "good" -> "达标"
                    "normal" -> "一般"
                    "poor" -> "未达标"
                    else -> "达标"
                }

                // 更新睡眠状态
                updateSleepStatus(it.sleepTime, it.wakeTime)
            }
        }
        
        // 移除睡眠记录观察，简化界面
    }
    
    /**
     * 更新睡眠状态 - 简化版本
     */
    private fun updateSleepStatus(sleepTime: String, wakeTime: String) {
        // 简化睡眠状态更新，移除不存在的视图引用
        // 只保留基本的时间显示逻辑
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadUserData()
        viewModel.loadTodaySleepRecord()
    }
    
    private fun showWelcomeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_welcome, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // 设置弹窗背景透明
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 找到关闭按钮并设置点击事件
        val closeButton = dialogView.findViewById<ImageView>(R.id.ivClose)
        closeButton?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // 3秒后自动关闭
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }

    override fun onPause() {
        super.onPause()
        stopCountdownTimer()
    }

    override fun onResume() {
        super.onResume()
        startCountdownTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCountdownTimer()
        _binding = null
    }

    /**
     * 启动倒计时定时器
     */
    private fun startCountdownTimer() {
        countdownHandler = Handler(Looper.getMainLooper())
        countdownRunnable = object : Runnable {
            override fun run() {
                updateCountdown()
                countdownHandler?.postDelayed(this, 1000) // 每秒更新一次
            }
        }
        countdownHandler?.post(countdownRunnable!!)
    }

    /**
     * 停止倒计时定时器
     */
    private fun stopCountdownTimer() {
        countdownRunnable?.let { countdownHandler?.removeCallbacks(it) }
        countdownHandler = null
        countdownRunnable = null
    }

    /**
     * 更新倒计时显示
     */
    private fun updateCountdown() {
        try {
            val currentTime = Calendar.getInstance()
            val targetSleepTime = Calendar.getInstance()

            // 获取用户设置的睡觉时间，默认23:00
            val userData = viewModel.userData.value
            val sleepTimeStr = userData?.sleepTime ?: "23:00"

            // 解析睡觉时间
            val timeParts = sleepTimeStr.split(":")
            val hour = if (timeParts.size >= 2) timeParts[0].toIntOrNull() ?: 23 else 23
            val minute = if (timeParts.size >= 2) timeParts[1].toIntOrNull() ?: 0 else 0

            // 设置目标睡觉时间
            targetSleepTime.set(Calendar.HOUR_OF_DAY, hour)
            targetSleepTime.set(Calendar.MINUTE, minute)
            targetSleepTime.set(Calendar.SECOND, 0)
            targetSleepTime.set(Calendar.MILLISECOND, 0)

            // 如果当前时间已经过了今天的睡觉时间，则计算到明天的睡觉时间
            if (currentTime.after(targetSleepTime)) {
                targetSleepTime.add(Calendar.DAY_OF_MONTH, 1)
            }

            val timeDiff = targetSleepTime.timeInMillis - currentTime.timeInMillis

            if (timeDiff > 0) {
                val hours = timeDiff / (1000 * 60 * 60)
                val minutes = (timeDiff % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (timeDiff % (1000 * 60)) / 1000

                val countdownText = String.format("距离下次睡觉时间：%02d:%02d:%02d", hours, minutes, seconds)
                binding.tvCountdown.text = countdownText
            } else {
                binding.tvCountdown.text = "距离下次睡觉时间：现在就是睡觉时间！"
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "更新倒计时失败", e)
            binding.tvCountdown.text = "距离下次睡觉时间：计算中..."
        }
    }
}