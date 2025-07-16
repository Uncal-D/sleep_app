package com.example.sleepapp.ui.shop

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepapp.data.model.Product
import com.example.sleepapp.data.model.User
import com.example.sleepapp.data.model.ProductType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val auth = FirebaseAuth.getInstance()
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)

                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    _userData.postValue(user)
                    Log.d("ShopViewModel", "用户数据加载成功: ${user?.email}")
                }
            } catch (e: Exception) {
                Log.e("ShopViewModel", "加载用户数据失败", e)
                // 确保在主线程显示错误
                withContext(Dispatchers.Main) {
                    _userData.postValue(null)
                }
            }
        }
    }
    
    /**
     * 加载商品列表
     */
    fun loadProducts() {
        Log.d("ShopViewModel", "loadProducts called")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 先在主线程显示默认商品
                withContext(Dispatchers.Main) {
                    val defaultProducts = getDefaultProducts()
                    _products.postValue(defaultProducts)
                }

                // 在IO线程从Firebase加载
                val snapshot = firestore.collection("products").get().await()
                val products = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }

                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    if (products.isNotEmpty()) {
                        _products.postValue(products)
                    } else {
                        Log.d("ShopViewModel", "使用默认商品列表")
                    }
                }
            } catch (e: Exception) {
                Log.e("ShopViewModel", "商品加载异常", e)
                // 确保在主线程处理错误
                withContext(Dispatchers.Main) {
                    // 保持默认商品显示
                }
            }
        }
    }

    private fun getDefaultProducts(): List<Product> {
        return listOf(
            Product(
                id = "default_1",
                name = "精美壁纸包",
                description = "包含10张高清壁纸",
                imageUrl = "",
                price = 50,
                type = ProductType.VIRTUAL_WALLPAPER
            ),
            Product(
                id = "default_2",
                name = "白噪音音效包",
                description = "助眠白噪音合集",
                imageUrl = "",
                price = 80,
                type = ProductType.VIRTUAL_SOUND
            ),
            Product(
                id = "default_3",
                name = "咖啡优惠券",
                description = "星巴克咖啡8折优惠券",
                imageUrl = "",
                price = 100,
                type = ProductType.COUPON
            )
        )
    }


    /**
     * 兑换商品
     */
    fun redeemProduct(product: Product) {
        Log.d("ShopViewModel", "开始兑换商品: ${product.name}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = _userData.value ?: run {
                    withContext(Dispatchers.Main) {
                        _redeemResult.postValue(RedeemResult.Error("用户数据未加载"))
                    }
                    return@launch
                }

                if (user.totalPoints < product.price) {
                    withContext(Dispatchers.Main) {
                        _redeemResult.postValue(RedeemResult.NotEnoughPoints)
                    }
                    return@launch
                }

                // 在IO线程执行Firebase操作
                val newPoints = user.totalPoints - product.price
                firestore.collection("users").document(user.id)
                    .update("totalPoints", newPoints).await()

                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    _redeemResult.postValue(RedeemResult.Success)
                    loadUserData()
                }

            } catch (e: Exception) {
                Log.e("ShopViewModel", "商品兑换异常", e)
                withContext(Dispatchers.Main) {
                    _redeemResult.postValue(RedeemResult.Error("兑换失败: ${e.message}"))
                }
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