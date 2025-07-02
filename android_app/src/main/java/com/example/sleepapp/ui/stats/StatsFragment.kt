package com.example.sleepapp.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sleepapp.databinding.FragmentStatsBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * 统计页面Fragment
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: StatsViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this).get(StatsViewModel::class.java)
        
        setupObservers()
        setupDatePickers()
    }
    
    private fun setupObservers() {
        // 观察统计数据
        viewModel.statsData.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                // 更新成功率
                val successRate = if (it.totalDays > 0) {
                    (it.successDays.toFloat() / it.totalDays) * 100
                } else {
                    0f
                }
                binding.tvSuccessRate.text = String.format("成功率: %.1f%%", successRate)
                binding.tvTotalDays.text = "总天数: ${it.totalDays}"
                binding.tvSuccessDays.text = "成功天数: ${it.successDays}"
                
                // 更新积分统计
                binding.tvTotalPoints.text = "总积分: ${it.totalPoints}"
                binding.tvSleepPoints.text = "睡前积分: ${it.sleepPoints}"
                binding.tvWakePoints.text = "起床积分: ${it.wakePoints}"
                binding.tvBonusPoints.text = "奖励积分: ${it.bonusPoints}"
                
                // 更新最长连续天数
                binding.tvMaxStreak.text = "最长连续天数: ${it.maxStreak}"
                
                // 更新进度条
                binding.progressBarSuccess.progress = successRate.toInt()
            }
        }
    }
    
    private fun setupDatePickers() {
        // 设置默认日期范围（过去一周）
        val endDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.DAY_OF_MONTH, -7)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.etStartDate.setText(dateFormat.format(startDate.time))
        binding.etEndDate.setText(dateFormat.format(endDate.time))
        
        // 加载初始统计数据
        viewModel.loadStatsData(startDate.time, endDate.time)
        
        // 设置日期选择器
        binding.etStartDate.setOnClickListener {
            showDatePicker(true)
        }
        
        binding.etEndDate.setOnClickListener {
            showDatePicker(false)
        }
        
        // 查询按钮
        binding.btnQuery.setOnClickListener {
            try {
                val start = dateFormat.parse(binding.etStartDate.text.toString())
                val end = dateFormat.parse(binding.etEndDate.text.toString())
                
                if (start != null && end != null) {
                    viewModel.loadStatsData(start, end)
                }
            } catch (e: Exception) {
                // 日期格式错误
            }
        }
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        try {
            val dateText = if (isStartDate) {
                binding.etStartDate.text.toString()
            } else {
                binding.etEndDate.text.toString()
            }
            
            val date = dateFormat.parse(dateText)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // 使用当前日期
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = dateFormat.format(calendar.time)
                
                if (isStartDate) {
                    binding.etStartDate.setText(selectedDate)
                } else {
                    binding.etEndDate.setText(selectedDate)
                }
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 