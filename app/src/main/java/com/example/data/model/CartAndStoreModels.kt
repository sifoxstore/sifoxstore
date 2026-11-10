package com.example.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedSize: String = "M",
    val selectedColor: String = "Black"
) {
    val subtotal: Double
        get() = product.effectivePrice * quantity
}

data class Coupon(
    val code: String,
    val discountPercent: Int = 0,
    val flatDiscount: Double = 0.0,
    val minSpend: Double = 0.0,
    val description: String = "",
    val isActive: Boolean = true
) {
    fun calculateDiscount(subtotal: Double): Double {
        if (!isActive || subtotal < minSpend) return 0.0
        return if (discountPercent > 0) {
            subtotal * (discountPercent.toDouble() / 100.0)
        } else {
            flatDiscount.coerceAtMost(subtotal)
        }
    }
}

data class StoreSettings(
    val storeName: String = "SiFox Store",
    val storeTagline: String = "Premium E-commerce Platform Bangladesh",
    val whatsappNumber: String = "+966530197252",
    val supportEmail: String = "sifoxstore@gmail.com",
    val insideDhakaDeliveryFee: Double = 80.0,
    val outsideDhakaDeliveryFee: Double = 140.0,
    val announcementText: String = "🔥 Super Sale is LIVE! Flat ৳80 Delivery inside Dhaka, ৳140 Nationwide. Use code SIFOX10!"
)
