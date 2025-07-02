package com.example.sleepapp.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepapp.data.local.AppDatabase
import com.example.sleepapp.data.model.Product
import com.example.sleepapp.data.model.ProductType
import com.example.sleepapp.data.model.User
import com.example.sleepapp.data.repository.SleepRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * 兑换结果
 */
sealed class RedeemResult {
    object Success : RedeemResult()
    object NotEnoughPoints : RedeemResult()
    data class Error(val message: String) : RedeemResult()
}

/**
 * 商城页面ViewModel
 */
class ShopViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sleepRecordDao = AppDatabase.getDatabase(application).sleepRecordDao()
    private val repository = SleepRepositoryImpl()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData
    
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    private val _redeemResult = MutableLiveData<RedeemResult?>()
    val redeemResult: LiveData<RedeemResult?> = _redeemResult
    
    init {
        loadUserData()
        loadProducts()
    }
    
    /**
     * 加载用户数据
     */
    fun loadUserData() {
        viewModelScope.launch {
            val user = repository.getUserData()
            _userData.postValue(user)
        }
    }
    
    /**
     * 加载商品列表
     */
    fun loadProducts() {
        viewModelScope.launch {
            try {
                // 从Firestore加载商品
                val productsCollection = firestore.collection("products")
                val snapshot = productsCollection.get().await()
                
                val productList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)
                }
                
                _products.postValue(productList)
            } catch (e: Exception) {
                // 如果加载失败，使用默认商品列表
                _products.postValue(getDefaultProducts())
            }
        }
    }
    
    /**
     * 获取默认商品列表
     */
    private fun getDefaultProducts(): List<Product> {
        return listOf(
            Product(
                id = "#001",
                name = "星空壁纸",
                description = "精美星空壁纸，让你的手机与众不同",
                imageUrl = "https://example.com/wallpaper1.jpg",
                price = 20,
                type = ProductType.VIRTUAL_WALLPAPER
            ),
            Product(
                id = "#002",
                name = "森林音效包",
                description = "自然森林音效，助你入眠",
                imageUrl = "https://example.com/sound1.jpg",
                price = 50,
                type = ProductType.VIRTUAL_SOUND
            ),
            Product(
                id = "#003",
                name = "咖啡优惠券",
                description = "任意咖啡店可用的5元优惠券",
                imageUrl = "https://example.com/coffee.jpg",
                price = 200,
                type = ProductType.COUPON
            ),
            Product(
                id = "#004",
                name = "睡眠眼罩",
                description = "高品质睡眠眼罩，遮光效果好",
                imageUrl = "https://example.com/eyemask.jpg",
                price = 300,
                type = ProductType.PHYSICAL_ITEM
            )
        )
    }
    
    /**
     * 兑换商品
     */
    fun redeemProduct(product: Product) {
        viewModelScope.launch {
            val user = _userData.value ?: return@launch
            
            // 检查积分是否足够
            if (user.totalPoints < product.price) {
                _redeemResult.postValue(RedeemResult.NotEnoughPoints)
                return@launch
            }
            
            try {
                // 扣除积分
                val newPoints = user.totalPoints - product.price
                
                // 更新用户积分
                val userRef = firestore.collection("users").document(user.userId)
                userRef.update("totalPoints", newPoints).await()
                
                // 记录兑换记录
                val redemption = hashMapOf(
                    "userId" to user.userId,
                    "productId" to product.id,
                    "productName" to product.name,
                    "pointsCost" to product.price,
                    "timestamp" to System.currentTimeMillis()
                )
                
                firestore.collection("redemptions").add(redemption).await()
                
                // 更新本地用户数据
                user.totalPoints = newPoints
                _userData.postValue(user)
                
                _redeemResult.postValue(RedeemResult.Success)
            } catch (e: Exception) {
                _redeemResult.postValue(RedeemResult.Error(e.message ?: "未知错误"))
            }
        }
    }
    
    /**
     * 重置兑换结果
     */
    fun resetRedeemResult() {
        _redeemResult.value = null
    }
} 