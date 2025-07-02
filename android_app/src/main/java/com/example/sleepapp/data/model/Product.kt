package com.example.sleepapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ProductType : Parcelable {
    VIRTUAL_WALLPAPER, VIRTUAL_SOUND, COUPON, PHYSICAL_ITEM
}

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Int, // 作为积分消耗字段
    val type: ProductType
) : Parcelable 