package com.example.sleepapp.ui.shop

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.sleepapp.data.model.Product
import com.example.sleepapp.databinding.DialogRedeemBinding

/**
 * 商品兑换对话框
 */
class RedeemDialogFragment : DialogFragment() {

    private var _binding: DialogRedeemBinding? = null
    private val binding get() = _binding!!
    
    private var onRedeemConfirmListener: (() -> Unit)? = null
    
    companion object {
        private const val ARG_PRODUCT = "product"
        private const val ARG_USER_POINTS = "user_points"
        
        fun newInstance(product: Product, userPoints: Int): RedeemDialogFragment {
            val fragment = RedeemDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_PRODUCT, product)
            args.putInt(ARG_USER_POINTS, userPoints)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRedeemBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val product = arguments?.getParcelable<Product>(ARG_PRODUCT)
        val userPoints = arguments?.getInt(ARG_USER_POINTS, 0) ?: 0
        if (product == null) {
            dismiss()
            return
        }
        
        setupUI(product, userPoints)
        setupListeners(userPoints, product)
    }
    
    private fun setupUI(product: Product, userPoints: Int) {
        binding.tvProductName.text = product.name
        binding.tvProductDescription.text = product.description
        binding.tvProductPoints.text = "${product.price} 积分"
        
        // 加载商品图片
        Glide.with(binding.ivProductImage.context)
            .load(product.imageUrl)
            .centerCrop()
            .into(binding.ivProductImage)
        
        // 判断积分是否足够
        if (userPoints < product.price) {
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.alpha = 0.5f
        } else {
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.alpha = 1.0f
        }
    }
    
    private fun setupListeners(userPoints: Int, product: Product) {
        binding.btnConfirm.setOnClickListener {
            if (userPoints >= product.price) {
                onRedeemConfirmListener?.invoke()
                dismiss()
            }
        }
        
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
    
    fun setOnRedeemConfirmListener(listener: () -> Unit) {
        onRedeemConfirmListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 