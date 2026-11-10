package com.example.data.model

enum class DeliveryZone(val title: String, val defaultFee: Double, val deliveryTime: String) {
    INSIDE_DHAKA("Inside Dhaka City", 80.0, "১ দিনের মধ্যে (Within 1 Day)"),
    OUTSIDE_DHAKA("Outside Dhaka City", 140.0, "২–৩ দিন (2–3 Days)")
}

enum class PaymentMethod(val title: String, val subtitle: String) {
    COD("Cash on Delivery (COD)", "Pay when you receive the parcel at your doorstep"),
    BKASH("bKash Merchant Payment", "Instant mobile wallet payment to 01700-SIFOX"),
    NAGAD("Nagad Merchant Payment", "Instant mobile wallet payment to 01800-SIFOX")
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    PENDING("Pending", 0),
    CONFIRMED("Confirmed", 1),
    PROCESSING("Processing", 2),
    SHIPPED("Shipped", 3),
    DELIVERED("Delivered", 4),
    CANCELLED("Cancelled", -1)
}

enum class PaymentStatus(val label: String) {
    PENDING("Pending"),
    VERIFIED("Verified & Paid"),
    FAILED("Failed")
}

data class Order(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val phone: String,
    val altPhone: String = "",
    val country: String = "Bangladesh",
    val division: String = "Dhaka",
    val district: String,
    val thana: String,
    val area: String,
    val fullAddress: String,
    val deliveryInstructions: String = "",
    val deliveryZone: DeliveryZone,
    val deliveryFee: Double,
    val deliveryTimeEstimate: String = deliveryZone.deliveryTime,
    val items: List<CartItem>,
    val subtotal: Double,
    val discount: Double = 0.0,
    val couponCode: String? = null,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val transactionId: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val trackingNumber: String = "",
    val courierPartner: String = "Steadfast Courier BD",
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String = "CUSTOMER", // "CUSTOMER", "ADMIN", "OWNER"
    val country: String = "Bangladesh",
    val division: String = "Dhaka",
    val district: String = "Dhaka",
    val thana: String = "Dhanmondi",
    val area: String = "Road 27",
    val fullAddress: String = "House 42, Road 27, Dhanmondi, Dhaka-1209"
)
