package com.example.sleepapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sleepapp.R
import com.example.sleepapp.databinding.ActivityMainBinding
import com.example.sleepapp.service.SleepMonitorService
import com.example.sleepapp.ui.auth.LoginActivity
import com.example.sleepapp.ui.home.HomeFragment
import com.example.sleepapp.ui.SettingsFragment
import com.example.sleepapp.ui.shop.ShopFragment
import com.example.sleepapp.ui.stats.StatsFragment
import com.google.firebase.auth.FirebaseAuth

/**
 * 主活动
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // private lateinit var usageStatsHelper: UsageStatsHelper // 暂时注释
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        // usageStatsHelper = UsageStatsHelper(this) // 暂时注释
        
        // 检查用户是否登录
        checkUserLogin()
        
        // 设置底部导航栏
        setupBottomNavigation()
        
        // 检查权限并启动服务
        // checkPermissionAndStartService() // 暂时注释
    }
    
    /**
     * 检查用户是否登录
     */
    private fun checkUserLogin() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    /**
     * 设置底部导航栏
     */
    private fun setupBottomNavigation() {
        // 默认显示主页
        loadFragment(HomeFragment())
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_stats -> loadFragment(StatsFragment())
                R.id.nav_shop -> loadFragment(ShopFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
                else -> false
            }
        }
    }
    
    /**
     * 加载Fragment
     */
    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
    
    /**
     * 检查权限并启动服务
     */
    // private fun checkPermissionAndStartService() {
    //     if (!usageStatsHelper.hasUsageStatsPermission()) {
    //         usageStatsHelper.openUsageAccessSettings()
    //     } else {
    //         startSleepMonitorService()
    //     }
    // }
    
    /**
     * 启动睡眠监控服务
     */
    // private fun startSleepMonitorService() {
    //     val serviceIntent = Intent(this, SleepMonitorService::class.java)
    //     startForegroundService(serviceIntent)
    // }
    
    override fun onResume() {
        super.onResume()
        // if (usageStatsHelper.hasUsageStatsPermission()) {
        //     startSleepMonitorService()
        // }
    }

    /**
     * 增加退出账号功能
     */
    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, com.example.sleepapp.ui.auth.LoginActivity::class.java))
        finish()
    }
} 