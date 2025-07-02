package com.example.sleepapp.ui.home

import android.os.Bundle
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

/**
 * 主页Fragment
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    
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
        
        setupObservers()
    }
    
    private fun setupObservers() {
        // 观察用户数据
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvPoints.text = it.totalPoints.toString()
                binding.tvStreak.text = it.currentStreak.toString()
                
                // 更新睡眠时间
                binding.tvSleepTime.text = getString(R.string.sleep_time) + ": " + it.sleepTime
                binding.tvWakeTime.text = getString(R.string.wake_time) + ": " + it.wakeTime
                
                // 更新今日积分明细
                val todayPoints = it.sleepPointsToday + it.wakePointsToday + it.bonusPointsToday
                binding.tvTodayPoints.text = "今日积分: $todayPoints"
                binding.tvSleepPoints.text = "睡前积分: ${it.sleepPointsToday}"
                binding.tvWakePoints.text = "起床积分: ${it.wakePointsToday}"
                binding.tvBonusPoints.text = "奖励积分: ${it.bonusPointsToday}"
                
                // 更新睡眠状态
                updateSleepStatus(it.sleepTime, it.wakeTime)
            }
        }
        
        // 观察睡眠记录
        viewModel.todaySleepRecord.observe(viewLifecycleOwner) { record ->
            record?.let {
                // 更新睡眠状态复选框
                binding.checkboxSleep.isChecked = !it.isAlarmSnoozed
                binding.checkboxWake.isChecked = !it.isAlarmSnoozed
                
                // 如果在睡眠期间使用了手机，显示警告
                if (it.isPhoneUsed) {
                    binding.tvWarning.visibility = View.VISIBLE
                    binding.tvWarning.text = "警告：睡眠期间使用手机，积分已清零！"
                } else {
                    binding.tvWarning.visibility = View.GONE
                }
            }
        }
    }
    
    /**
     * 更新睡眠状态
     */
    private fun updateSleepStatus(sleepTime: String, wakeTime: String) {
        val now = System.currentTimeMillis()
        val sleepTimeMillis = getTodaySleepTimeMillis(sleepTime)
        val wakeTimeMillis = getTodayWakeTimeMillis(wakeTime)
        
        val status = when {
            now < sleepTimeMillis - 30 * 60 * 1000 -> R.string.awake
            now < sleepTimeMillis -> R.string.sleep_soon
            now < wakeTimeMillis -> R.string.sleeping
            else -> R.string.awake
        }
        
        binding.tvSleepStatus.text = getString(status)
        
        // 更新进度条
        val totalDayTime = 24 * 60 * 60 * 1000
        val progress = ((now % totalDayTime) / totalDayTime.toFloat() * 100).toInt()
        binding.progressBar.progress = progress
        
        // 更新距离下一个时间点的倒计时
        when {
            now < sleepTimeMillis -> {
                val timeLeft = (sleepTimeMillis - now) / (60 * 1000)
                binding.tvTimeLeft.text = "距离睡觉时间还有 $timeLeft 分钟"
                binding.tvTimeLeft.visibility = View.VISIBLE
            }
            now < wakeTimeMillis -> {
                val timeLeft = (wakeTimeMillis - now) / (60 * 1000)
                binding.tvTimeLeft.text = "距离起床时间还有 $timeLeft 分钟"
                binding.tvTimeLeft.visibility = View.VISIBLE
            }
            else -> {
                binding.tvTimeLeft.visibility = View.GONE
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadUserData()
        viewModel.loadTodaySleepRecord()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 