package com.example.sleepapp.ui.shop

import android.os.Bundle
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
        
        viewModel = ViewModelProvider(this).get(ShopViewModel::class.java)
        
        setupRecyclerView()
        setupObservers()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            onProductClicked(product)
        }
        
        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ShopFragment.adapter
        }
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
            adapter.submitList(products)
            
            if (products.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
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
        // 显示兑换确认对话框，传递当前用户积分
        val dialog = RedeemDialogFragment.newInstance(product, currentUserPoints)
        dialog.setOnRedeemConfirmListener {
            viewModel.redeemProduct(product)
        }
        dialog.show(childFragmentManager, "redeem_dialog")
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadUserData()
        viewModel.loadProducts()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 