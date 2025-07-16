package com.example.sleepapp.ui.shop

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sleepapp.data.model.Product
import com.example.sleepapp.databinding.ItemProductBinding

/**
 * 商品列表适配器
 */
class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // 设置整个卡片的点击事件
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = getItem(position)
                    Log.d("ProductAdapter", "商品卡片被点击: ${product.name}, 价格: ${product.price}")
                    onProductClick(product)
                } else {
                    Log.w("ProductAdapter", "点击了无效位置: $position")
                }
            }

            // 设置兑换按钮的点击事件
            binding.btnRedeem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = getItem(position)
                    Log.d("ProductAdapter", "兑换按钮被点击: ${product.name}, 价格: ${product.price}")
                    onProductClick(product)
                } else {
                    Log.w("ProductAdapter", "兑换按钮点击了无效位置: $position")
                }
            }
        }

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductDescription.text = product.description
            binding.tvProductPoints.text = "${product.price} 积分"
            
            // 加载商品图片，如果URL为空则使用默认图片
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
        }
    }
}

/**
 * 商品差异比较回调
 */
class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
} 