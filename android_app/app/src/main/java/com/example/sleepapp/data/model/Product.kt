package com.example.sleepapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ProductType : Parcelable {
    VIRTUAL_WALLPAPER, VIRTUAL_SOUND, COUPON, PHYSICAL_ITEM
}

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val price: Int = 0,
    val type: ProductType = ProductType.VIRTUAL_WALLPAPER,
    val category: String = "",
    val stockQuantity: Int = -1,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    // 无参构造函数，Firebase需要
    constructor() : this(
        id = "",
        name = "",
        description = "",
        imageUrl = "",
        price = 0,
        type = ProductType.VIRTUAL_WALLPAPER,
        category = "",
        stockQuantity = -1,
        isActive = true,
        sortOrder = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}