package com.example.sleepapp.ui.shop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sleepapp.data.model.Product
import com.example.sleepapp.databinding.FragmentShopBinding

/**
 * 商城页面Fragment
 */
class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ShopViewModel
    private lateinit var adapter: ProductAdapter
    private var currentUserPoints: Int = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ShopFragment", "onViewCreated called")

        try {
            viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)
            Log.d("ShopFragment", "ShopViewModel initialized successfully")

            setupRecyclerView()
            setupObservers()
            Log.d("ShopFragment", "ShopFragment setup completed")
        } catch (e: Exception) {
            Log.e("ShopFragment", "Error in onViewCreated", e)
        }
    }
    
    private fun setupRecyclerView() {
        Log.d("ShopFragment", "Setting up RecyclerView")
        adapter = ProductAdapter { product ->
            Log.d("ShopFragment", "ProductAdapter callback triggered for: ${product.name}")
            onProductClicked(product)
        }

        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ShopFragment.adapter
        }
        Log.d("ShopFragment", "RecyclerView setup completed")
    }
    
    private fun setupObservers() {
        // 观察用户数据
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserPoints.text = "当前积分: ${it.totalPoints}"
                currentUserPoints = it.totalPoints
            }
        }
        
        // 观察商品列表
        viewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("ShopFragment", "Products loaded: ${products.size} items")
            adapter.submitList(products)

            if (products.isEmpty()) {
                Log.w("ShopFragment", "No products available")
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                Log.d("ShopFragment", "Products available, hiding empty view")
                binding.tvEmpty.visibility = View.GONE
            }
        }
        
        // 观察兑换结果
        viewModel.redeemResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is RedeemResult.Success -> {
                    Toast.makeText(context, "兑换成功！", Toast.LENGTH_SHORT).show()
                    // 刷新用户积分
                    viewModel.loadUserData()
                }
                is RedeemResult.NotEnoughPoints -> {
                    Toast.makeText(context, "积分不足，无法兑换", Toast.LENGTH_SHORT).show()
                }
                is RedeemResult.Error -> {
                    Toast.makeText(context, "兑换失败：${result.message}", Toast.LENGTH_SHORT).show()
                }
                null -> {}
            }
        }
    }
    
    private fun onProductClicked(product: Product) {
        Log.d("ShopFragment", "商品被点击: ${product.name}, 当前用户积分: $currentUserPoints")

        // 快速检查积分，避免无效操作
        if (currentUserPoints < product.price) {
            Toast.makeText(requireContext(), "积分不足，当前积分：$currentUserPoints，需要积分：${product.price}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 显示兑换确认对话框，传递当前用户积分
            val dialog = RedeemDialogFragment.newInstance(product, currentUserPoints)
            dialog.setOnRedeemConfirmListener {
                Log.d("ShopFragment", "用户确认兑换: ${product.name}")
                // 在后台线程执行兑换操作，避免阻塞UI
                viewModel.redeemProduct(product)
            }
            dialog.show(childFragmentManager, "redeem_dialog")
            Log.d("ShopFragment", "兑换对话框已显示")
        } catch (e: Exception) {
            Log.e("ShopFragment", "显示兑换对话框时出错", e)
            Toast.makeText(context, "打开兑换对话框失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("ShopFragment", "onResume called, loading data")
        viewModel.loadUserData()
        viewModel.loadProducts()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 