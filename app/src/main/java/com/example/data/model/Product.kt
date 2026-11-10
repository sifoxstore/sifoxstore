package com.example.data.model

data class Product(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val discountPrice: Double? = null,
    val stock: Int = 25,
    val description: String = "",
    val sizes: List<String> = listOf("S", "M", "L", "XL"),
    val colors: List<String> = listOf("Black", "Sky Blue", "White"),
    val imageUrl: String = "",
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val rating: Double = 4.8,
    val reviewCount: Int = 42
) {
    val effectivePrice: Double
        get() = discountPrice ?: price

    val hasDiscount: Boolean
        get() = discountPrice != null && discountPrice < price

    val discountPercentage: Int
        get() = if (hasDiscount) (((price - discountPrice!!) / price) * 100).toInt() else 0
}

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String = ""
)
