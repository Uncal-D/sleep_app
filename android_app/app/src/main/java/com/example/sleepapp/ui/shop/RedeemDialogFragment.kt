package com.example.sleepapp.ui.shop

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.sleepapp.data.model.Product
import com.example.sleepapp.databinding.DialogRedeemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

/**
 * 商品兑换对话框
 */
class RedeemDialogFragment : DialogFragment() {

    private var _binding: DialogRedeemBinding? = null
    private val binding get() = _binding!!
    
    private var onRedeemConfirmListener: (() -> Unit)? = null
    
    /**
     * 执行兑换操作
     */
    private fun performRedeem(product: Product, quantity: Int, totalCost: Int) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载中
        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.text = "处理中..."

        // 1. 扣除用户积分
        val userRef = firestore.collection("users").document(currentUser.uid)

        firestore.runTransaction { transaction ->
            // 获取最新的用户数据
            val userSnapshot = transaction.get(userRef)
            val currentPoints = userSnapshot.getLong("totalPoints")?.toInt() ?: 0

            // 检查积分是否足够
            if (currentPoints < totalCost) {
                throw Exception("积分不足")
            }

            // 扣除积分
            transaction.update(userRef, "totalPoints", currentPoints - totalCost)

            // 返回剩余积分
            currentPoints - totalCost
        }.addOnSuccessListener { remainingPoints ->
            // 2. 添加兑换记录
            val redeemRecord = hashMapOf(
                "userId" to currentUser.uid,
                "productId" to product.id,
                "productName" to product.name,
                "quantity" to quantity,
                "points" to totalCost,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("redemptions").add(redeemRecord)
                .addOnSuccessListener {
                    // 3. 添加积分流水记录
                    val pointsHistory = hashMapOf(
                        "userId" to currentUser.uid,
                        "points" to -totalCost,
                        "type" to "redeem",
                        "description" to "兑换商品: ${product.name} x $quantity",
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("pointsHistory").add(pointsHistory)
                        .addOnSuccessListener {
                            // 全部成功
                            Toast.makeText(context, "兑换成功！剩余积分: $remainingPoints", Toast.LENGTH_SHORT).show()
                            onRedeemConfirmListener?.invoke()
                            dismiss()
                        }
                        .addOnFailureListener { e ->
                            // 积分流水记录失败，但兑换已完成
                            Toast.makeText(context, "兑换成功！但记录保存失败", Toast.LENGTH_SHORT).show()
                            onRedeemConfirmListener?.invoke()
                            dismiss()
                        }
                }
                .addOnFailureListener { e ->
                    // 兑换记录失败，但积分已扣除
                    Toast.makeText(context, "兑换处理中，请稍后查看", Toast.LENGTH_SHORT).show()
                    onRedeemConfirmListener?.invoke()
                    dismiss()
                }
        }.addOnFailureListener { e ->
            // 积分扣除失败
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.text = "立即兑换"
            Toast.makeText(context, "兑换失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

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

        // 设置对话框样式
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

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
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(binding.ivProductImage.context)
                .load(product.imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivProductImage)
        } else {
            // 使用默认图片
            binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        
        // 判断积分是否足够
        if (userPoints < product.price) {
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.alpha = 0.5f
            Toast.makeText(context, "积分不足，无法兑换", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnConfirm.isEnabled = true
            binding.btnConfirm.alpha = 1.0f
        }

        // 设置关闭按钮
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
    
    private fun setupListeners(userPoints: Int, product: Product) {
        // 数量加减按钮
        var quantity = 1

        binding.ivMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.ivPlus.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }

        // 确认兑换按钮
        binding.btnConfirm.setOnClickListener {
            Log.d("RedeemDialogFragment", "确认兑换按钮被点击")
            val totalCost = product.price * quantity
            Log.d("RedeemDialogFragment", "商品: ${product.name}, 数量: $quantity, 总价: $totalCost, 用户积分: $userPoints")

            if (userPoints >= totalCost) {
                Log.d("RedeemDialogFragment", "积分足够，开始执行兑换操作")
                // 执行兑换操作
                performRedeem(product, quantity, totalCost)
            } else {
                Log.w("RedeemDialogFragment", "积分不足，无法兑换")
                Toast.makeText(context, "积分不足，无法兑换", Toast.LENGTH_SHORT).show()
            }
        }

        // 取消按钮
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