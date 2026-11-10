package com.example.data.repository

import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Coupon
import com.example.data.model.DeliveryZone
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object StoreRepository {

    val bangladeshDivisions = listOf(
        "Dhaka",
        "Chittagong",
        "Rajshahi",
        "Khulna",
        "Barishal",
        "Sylhet",
        "Rangpur",
        "Mymensingh"
    )

    val bangladeshDistrictsByDivision: Map<String, List<String>> = mapOf(
        "Dhaka" to listOf(
            "Dhaka", "Gazipur", "Narayanganj", "Tangail", "Narsingdi",
            "Kishoreganj", "Manikganj", "Munshiganj", "Faridpur", "Gopalganj",
            "Madaripur", "Rajbari", "Shariatpur"
        ),
        "Chittagong" to listOf(
            "Chittagong", "Cox's Bazar", "Cumilla", "Feni", "Brahmanbaria",
            "Noakhali", "Chandpur", "Lakshmipur", "Rangamati", "Khagrachhari", "Bandarban"
        ),
        "Rajshahi" to listOf(
            "Rajshahi", "Bogura", "Pabna", "Sirajganj", "Naogaon",
            "Natore", "Chapainawabganj", "Joypurhat"
        ),
        "Khulna" to listOf(
            "Khulna", "Jashore", "Kushtia", "Jhenaidah", "Satkhira",
            "Bagerhat", "Chuadanga", "Meherpur", "Narail", "Magura"
        ),
        "Barishal" to listOf(
            "Barishal", "Patuakhali", "Bhola", "Pirojpur", "Barguna", "Jhalokathi"
        ),
        "Sylhet" to listOf(
            "Sylhet", "Moulvibazar", "Habiganj", "Sunamganj"
        ),
        "Rangpur" to listOf(
            "Rangpur", "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat",
            "Nilphamari", "Panchagarh", "Thakurgaon"
        ),
        "Mymensingh" to listOf(
            "Mymensingh", "Jamalpur", "Netrokona", "Sherpur"
        )
    )

    val allBangladeshDistricts: List<String> = bangladeshDistrictsByDivision.values.flatten().sorted()

    fun calculateDeliveryZone(district: String): DeliveryZone {
        val clean = district.trim().lowercase()
        return if (clean == "dhaka" || clean == "dhaka city" || clean.startsWith("dhaka")) {
            DeliveryZone.INSIDE_DHAKA
        } else {
            DeliveryZone.OUTSIDE_DHAKA
        }
    }

    private val initialConnectedWebsites = listOf(
        com.example.data.model.ConnectedWebsite(
            id = "web_1",
            title = "SiFox Official Web Portal",
            url = "https://sifoxstore.com",
            category = "E-Commerce",
            iconEmoji = "🦊",
            description = "Official SiFox Store Bangladesh online marketplace with full catalogue and order sync.",
            isOpenInApp = true,
            isPublic = true,
            visitCount = 1420
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_2",
            title = "Steadfast Courier BD",
            url = "https://steadfast.com.bd",
            category = "Logistics",
            iconEmoji = "🚚",
            description = "Direct parcel booking, live dispatch tracking, and nationwide cash on delivery logistics.",
            isOpenInApp = true,
            isPublic = true,
            visitCount = 980
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_3",
            title = "Pathao Parcel & Express",
            url = "https://pathao.com",
            category = "Logistics",
            iconEmoji = "📦",
            description = "Same-day on-demand Dhaka express deliveries and parcel tracking.",
            isOpenInApp = true,
            isPublic = true,
            visitCount = 675
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_4",
            title = "bKash Merchant Portal",
            url = "https://www.bkash.com",
            category = "Payments",
            iconEmoji = "💳",
            description = "Online bKash transaction confirmation, payment verification, and statement lookup.",
            isOpenInApp = true,
            isPublic = true,
            visitCount = 1120
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_5",
            title = "Nagad Online BD",
            url = "https://nagad.com.bd",
            category = "Payments",
            iconEmoji = "⚡",
            description = "Nagad merchant gateway portal for order transaction settlement and receipt generation.",
            isOpenInApp = true,
            isPublic = true,
            visitCount = 540
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_6",
            title = "Bangladesh Post Office (e-Post)",
            url = "https://bdpost.gov.bd",
            category = "Government",
            iconEmoji = "📮",
            description = "Bangladesh postal service EMS parcel tracking and postal zip code lookup across 64 districts.",
            isOpenInApp = true,
            isPublic = true,
            visitCount = 310
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_7",
            title = "SiFox Cloud Admin Central",
            url = "https://admin.sifoxstore.com",
            category = "Cloud & Admin",
            iconEmoji = "⚙️",
            description = "Super admin cloud portal for inventory syncing, customer records, and platform telemetry.",
            isOpenInApp = true,
            isPublic = false,
            visitCount = 890
        ),
        com.example.data.model.ConnectedWebsite(
            id = "web_8",
            title = "Google Workspace & Docs",
            url = "https://drive.google.com",
            category = "Productivity",
            iconEmoji = "📁",
            description = "Shared spreadsheets for stock inventory, supplier invoices, and accounting audits.",
            isOpenInApp = false,
            isPublic = true,
            visitCount = 430
        )
    )

    private val _connectedWebsites = MutableStateFlow<List<com.example.data.model.ConnectedWebsite>>(initialConnectedWebsites)
    val connectedWebsites: StateFlow<List<com.example.data.model.ConnectedWebsite>> = _connectedWebsites.asStateFlow()

    fun addConnectedWebsite(website: com.example.data.model.ConnectedWebsite) {
        _connectedWebsites.value = listOf(website) + _connectedWebsites.value
    }

    fun updateConnectedWebsite(website: com.example.data.model.ConnectedWebsite) {
        _connectedWebsites.value = _connectedWebsites.value.map {
            if (it.id == website.id) website else it
        }
    }

    fun deleteConnectedWebsite(id: String) {
        _connectedWebsites.value = _connectedWebsites.value.filterNot { it.id == id }
    }

    fun recordWebsiteVisit(id: String) {
        _connectedWebsites.value = _connectedWebsites.value.map {
            if (it.id == id) it.copy(visitCount = it.visitCount + 1) else it
        }
    }

    val categories = listOf(
        Category("1", "Men's Fashion", "checkroom", "Shirts, Panjabis, Pants, Hoodies & Footwear"),
        Category("2", "Women's Fashion", "styler", "Sarees, Kurtis, Abayas & Western Wear"),
        Category("3", "Gadgets & Electronics", "devices", "Smart Watches, Earbuds, Power Banks & Audio"),
        Category("4", "Lifestyle Products", "spa", "Wallets, Travel Gear, Mugs & Daily Essentials"),
        Category("5", "Accessories", "watch", "Sunglasses, Belts, Caps, Rings & Bracelets"),
        Category("6", "Beauty & Personal Care", "face", "Skincare, Beard Grooming, Perfumes & Hair Care"),
        Category("7", "Home & Living", "cottage", "Decor, Smart Lighting, Bedding & Kitchenware"),
        Category("8", "Sports & Fitness", "fitness_center", "Gym Apparel, Resistance Bands & Bottles"),
        Category("9", "Kids & Baby Products", "child_care", "Baby Clothing, Soft Toys & Nursery Care"),
        Category("10", "Special Offers", "local_fire_department", "Mega Flash Deals, B1G1 & 50% Off Sales")
    )

    private val initialProducts = listOf(
        Product(
            id = "prod_1",
            title = "SiFox Edition Premium Smart Watch Series 9",
            category = "Gadgets & Electronics",
            price = 4200.0,
            discountPrice = 2850.0,
            stock = 38,
            description = "High definition AMOLED screen, Bluetooth HD calling, heart rate & SpO2 health tracking, IP68 waterproof and 7-day battery life. 1-year official replacement warranty.",
            sizes = listOf("45mm", "49mm Ultra"),
            colors = listOf("Midnight Black", "Sky Blue", "Silver Frost"),
            imageUrl = "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&q=80",
            isFeatured = true,
            rating = 4.9,
            reviewCount = 142
        ),
        Product(
            id = "prod_2",
            title = "SiFox Signature Royal Oxford Cotton Shirt",
            category = "Men's Fashion",
            price = 2200.0,
            discountPrice = 1450.0,
            stock = 54,
            description = "100% long-staple combed cotton woven in a textured pinpoint Oxford weave. Wrinkle-resistant finish for everyday office elegance and evening style.",
            sizes = listOf("M (38)", "L (40)", "XL (42)", "XXL (44)"),
            colors = listOf("Sky Blue", "Pure White", "Navy Blue"),
            imageUrl = "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&q=80",
            isFeatured = true,
            rating = 4.8,
            reviewCount = 98
        ),
        Product(
            id = "prod_3",
            title = "Acoustic Pro Wireless Active Noise Canceling Headphones",
            category = "Gadgets & Electronics",
            price = 5500.0,
            discountPrice = 3890.0,
            stock = 22,
            description = "40mm custom high-fidelity neodymium drivers, -35dB hybrid ANC, low latency gaming mode, plush memory foam earcups, 50 hours playtime.",
            sizes = listOf("Standard"),
            colors = listOf("Matte Black", "Ice White", "Sky Titanium"),
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80",
            isFeatured = true,
            rating = 4.9,
            reviewCount = 210
        ),
        Product(
            id = "prod_4",
            title = "Premium Artisanal Embroidered Jamdani Silk Kurti",
            category = "Women's Fashion",
            price = 3500.0,
            discountPrice = 2490.0,
            stock = 19,
            description = "Exquisite hand-finished Dhakai Jamdani motifs on pure fine breathable silk blend. Designed for festive celebrations, Eid, and formal gatherings.",
            sizes = listOf("36", "38", "40", "42"),
            colors = listOf("Sky Blue & Gold", "Pastel Lilac", "Ruby Red"),
            imageUrl = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800&q=80",
            isFeatured = true,
            rating = 4.7,
            reviewCount = 67
        ),
        Product(
            id = "prod_5",
            title = "Urban Commuter Waterproof Laptop Backpack 25L",
            category = "Lifestyle Products",
            price = 2800.0,
            discountPrice = 1850.0,
            stock = 45,
            description = "Heavy-duty 900D water-repellent oxford fabric with dedicated 16-inch fleece laptop compartment, anti-theft hidden pocket, and luggage strap.",
            sizes = listOf("25 Liters"),
            colors = listOf("Carbon Gray", "Sky Blue Trim", "Deep Black"),
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80",
            isFeatured = true,
            rating = 4.8,
            reviewCount = 115
        ),
        Product(
            id = "prod_6",
            title = "Classic Aviator Polarized Sunglasses UV400",
            category = "Accessories",
            price = 1600.0,
            discountPrice = 990.0,
            stock = 60,
            description = "Ultra-lightweight alloy metal frame with TAC polarized 9-layer lenses that block 100% of harmful UVA/UVB rays and glare.",
            sizes = listOf("Free Size"),
            colors = listOf("Silver Frame / Sky Mirror", "Black Frame / Dark Grey", "Gold Frame / Amber"),
            imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&q=80",
            isFeatured = false,
            rating = 4.6,
            reviewCount = 52
        ),
        Product(
            id = "prod_7",
            title = "FoxPure Organic Tea Tree & Niacinamide Glowing Serum",
            category = "Beauty & Personal Care",
            price = 1450.0,
            discountPrice = 980.0,
            stock = 34,
            description = "Clinically formulated with 10% pure Niacinamide, 2% Zinc PCA, and calming Australian Tea Tree. Clears acne marks and refines pores in 14 days.",
            sizes = listOf("30ml Bottle"),
            colors = listOf("Clear"),
            imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800&q=80",
            isFeatured = false,
            rating = 4.8,
            reviewCount = 89
        ),
        Product(
            id = "prod_8",
            title = "Nordic Minimalist LED Ambient Touch Lamp",
            category = "Home & Living",
            price = 2400.0,
            discountPrice = 1690.0,
            stock = 28,
            description = "Sleek wireless rechargeable table lamp with 3 color temperatures (Warm, Natural, Cool White) and smooth stepless dimming for bedrooms and living rooms.",
            sizes = listOf("Medium (28cm)"),
            colors = listOf("Matte White", "Sky Blue", "Brushed Gold"),
            imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&q=80",
            isFeatured = false,
            rating = 4.7,
            reviewCount = 43
        ),
        Product(
            id = "prod_9",
            title = "ProFit High-Bounce Running Shoes Lightweight",
            category = "Sports & Fitness",
            price = 3800.0,
            discountPrice = 2450.0,
            stock = 32,
            description = "Engineered breathable mesh upper paired with cloud-foam nitrogen-cushioned midsole for high energy return on roads and treadmills.",
            sizes = listOf("40 (7)", "41 (8)", "42 (8.5)", "43 (9.5)", "44 (10)"),
            colors = listOf("Sky Blue & White", "Triple Black", "Wolf Grey"),
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
            isFeatured = true,
            rating = 4.9,
            reviewCount = 178
        ),
        Product(
            id = "prod_10",
            title = "100% Organic Bamboo Cotton Baby Romper Set (Pack of 3)",
            category = "Kids & Baby Products",
            price = 1800.0,
            discountPrice = 1250.0,
            stock = 40,
            description = "Hypoallergenic, ultra-soft breathable bamboo fabric with nickel-free snap buttons for easy diaper changes. Safe for sensitive newborn skin.",
            sizes = listOf("0-3 Months", "3-6 Months", "6-12 Months"),
            colors = listOf("Pastel Blue & Clouds", "Mint Animals", "Pure Oatmeal"),
            imageUrl = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800&q=80",
            isFeatured = false,
            rating = 4.9,
            reviewCount = 64
        ),
        Product(
            id = "prod_11",
            title = "Mega Bundle: Wireless ANC Earbuds + 20000mAh Fast Powerbank",
            category = "Special Offers",
            price = 4900.0,
            discountPrice = 2990.0,
            stock = 15,
            description = "Exclusive SiFox Deal! Get high-res Bluetooth 5.4 wireless earbuds paired with 22.5W Super Charge dual USB-C battery bank. Save ৳1,910 today!",
            sizes = listOf("Combo Pack"),
            colors = listOf("Sky Blue Edition", "Jet Black Duo"),
            imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&q=80",
            isFeatured = true,
            rating = 5.0,
            reviewCount = 88
        )
    )

    private val initialCoupons = listOf(
        Coupon(
            code = "SIFOX10",
            discountPercent = 10,
            minSpend = 1000.0,
            description = "10% OFF on all orders above ৳1,000"
        ),
        Coupon(
            code = "WELCOME500",
            flatDiscount = 500.0,
            minSpend = 3000.0,
            description = "Flat ৳500 OFF on orders above ৳3,000"
        ),
        Coupon(
            code = "DHAKA20",
            discountPercent = 20,
            minSpend = 2000.0,
            description = "20% Special Promotion for BD shoppers"
        )
    )

    // Reactive states
    private val _products = MutableStateFlow<List<Product>>(initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(
        listOf(
            CartItem(initialProducts[0], quantity = 1, selectedSize = "45mm", selectedColor = "Midnight Black"),
            CartItem(initialProducts[1], quantity = 1, selectedSize = "L (40)", selectedColor = "Sky Blue")
        )
    )
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _wishlistIds = MutableStateFlow<Set<String>>(setOf("prod_1", "prod_3", "prod_9"))
    val wishlistIds: StateFlow<Set<String>> = _wishlistIds.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(initialCoupons)
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<Coupon?>(initialCoupons[0])
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _storeSettings = MutableStateFlow(StoreSettings())
    val storeSettings: StateFlow<StoreSettings> = _storeSettings.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile(
        id = "usr_1",
        name = "Sifat Mozumder",
        email = "mdsifatmozumder07@gmail.com",
        phone = "01712345678",
        role = "CUSTOMER",
        district = "Dhaka",
        thana = "Dhanmondi",
        area = "Road 27, House 42",
        fullAddress = "House 42, Road 27, Dhanmondi, Dhaka 1209"
    ))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(
        listOf(
            Order(
                id = "ord_101",
                orderNumber = "SF-74291",
                customerName = "Sifat Mozumder",
                phone = "01712345678",
                altPhone = "01887654321",
                district = "Dhaka",
                thana = "Dhanmondi",
                area = "Road 27",
                fullAddress = "House 42, Road 27, Dhanmondi, Dhaka 1209",
                deliveryInstructions = "Please call before arriving",
                deliveryZone = DeliveryZone.INSIDE_DHAKA,
                deliveryFee = 80.0,
                items = listOf(
                    CartItem(initialProducts[2], quantity = 1, selectedSize = "Standard", selectedColor = "Sky Titanium")
                ),
                subtotal = 3890.0,
                discount = 389.0,
                couponCode = "SIFOX10",
                total = 3581.0,
                paymentMethod = PaymentMethod.BKASH,
                transactionId = "9K88F4A2",
                paymentStatus = PaymentStatus.VERIFIED,
                orderStatus = OrderStatus.SHIPPED,
                trackingNumber = "SFBD-9923841",
                courierPartner = "Steadfast Courier BD",
                createdAt = System.currentTimeMillis() - (86400000L * 2)
            ),
            Order(
                id = "ord_102",
                orderNumber = "SF-88314",
                customerName = "Tanvir Ahmed",
                phone = "01998765432",
                altPhone = "",
                district = "Chittagong",
                thana = "Panchlaish",
                area = "GEC Circle",
                fullAddress = "Flat 4B, Green Tower, GEC Circle, Chittagong",
                deliveryInstructions = "Leave with security if not available",
                deliveryZone = DeliveryZone.OUTSIDE_DHAKA,
                deliveryFee = 140.0,
                items = listOf(
                    CartItem(initialProducts[4], quantity = 1, selectedSize = "25 Liters", selectedColor = "Sky Blue Trim")
                ),
                subtotal = 1850.0,
                discount = 0.0,
                couponCode = null,
                total = 1990.0,
                paymentMethod = PaymentMethod.COD,
                transactionId = "",
                paymentStatus = PaymentStatus.PENDING,
                orderStatus = OrderStatus.PROCESSING,
                trackingNumber = "SFBD-771234",
                courierPartner = "RedX Delivery BD",
                createdAt = System.currentTimeMillis() - 86400000L
            )
        )
    )
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Cart Actions
    fun addToCart(product: Product, size: String, color: String, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst {
            it.product.id == product.id && it.selectedSize == size && it.selectedColor == color
        }
        if (index >= 0) {
            val existing = current[index]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stock)
            current[index] = existing.copy(quantity = newQty)
        } else {
            current.add(CartItem(product, quantity.coerceAtMost(product.stock), size, color))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(cartIndex: Int, delta: Int) {
        val current = _cartItems.value.toMutableList()
        if (cartIndex in current.indices) {
            val item = current[cartIndex]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(cartIndex)
            } else {
                current[cartIndex] = item.copy(quantity = newQty.coerceAtMost(item.product.stock))
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(cartIndex: Int) {
        val current = _cartItems.value.toMutableList()
        if (cartIndex in current.indices) {
            current.removeAt(cartIndex)
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Wishlist Actions
    fun toggleWishlist(productId: String) {
        val current = _wishlistIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _wishlistIds.value = current
    }

    // Coupon Actions
    fun applyCoupon(code: String, subtotal: Double): Pair<Boolean, String> {
        val coupon = _coupons.value.find { it.code.equals(code.trim(), ignoreCase = true) && it.isActive }
        if (coupon == null) {
            return Pair(false, "Invalid coupon code.")
        }
        if (subtotal < coupon.minSpend) {
            return Pair(false, "Minimum order amount of ৳${coupon.minSpend.toInt()} required for this coupon.")
        }
        _appliedCoupon.value = coupon
        return Pair(true, "Coupon '${coupon.code}' applied successfully!")
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun addCoupon(coupon: Coupon) {
        _coupons.value = _coupons.value + coupon
    }

    fun deleteCoupon(code: String) {
        _coupons.value = _coupons.value.filterNot { it.code == code }
        if (_appliedCoupon.value?.code == code) {
            _appliedCoupon.value = null
        }
    }

    // Order Actions
    fun placeOrder(
        customerName: String,
        phone: String,
        altPhone: String,
        country: String = "Bangladesh",
        division: String = "Dhaka",
        district: String,
        thana: String,
        area: String,
        fullAddress: String,
        deliveryInstructions: String,
        deliveryZone: DeliveryZone,
        paymentMethod: PaymentMethod,
        transactionId: String = ""
    ): Order {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.subtotal }
        val discount = _appliedCoupon.value?.calculateDiscount(subtotal) ?: 0.0
        val deliveryFee = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) {
            _storeSettings.value.insideDhakaDeliveryFee
        } else {
            _storeSettings.value.outsideDhakaDeliveryFee
        }
        val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

        val uniqueNumber = "SF-" + (10000 + (Math.random() * 89999).toInt())
        val isPrepaid = paymentMethod != PaymentMethod.COD && transactionId.isNotBlank()

        val newOrder = Order(
            id = UUID.randomUUID().toString(),
            orderNumber = uniqueNumber,
            customerName = customerName,
            phone = phone,
            altPhone = altPhone,
            country = country,
            division = division,
            district = district,
            thana = thana,
            area = area,
            fullAddress = fullAddress,
            deliveryInstructions = deliveryInstructions,
            deliveryZone = deliveryZone,
            deliveryFee = deliveryFee,
            deliveryTimeEstimate = deliveryZone.deliveryTime,
            items = items,
            subtotal = subtotal,
            discount = discount,
            couponCode = _appliedCoupon.value?.code,
            total = total,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            paymentStatus = if (isPrepaid) PaymentStatus.VERIFIED else PaymentStatus.PENDING,
            orderStatus = OrderStatus.CONFIRMED,
            trackingNumber = "SFBD-" + (100000 + (Math.random() * 899999).toInt()),
            courierPartner = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) "Pathao / Steadfast Express" else "Steadfast Courier BD",
            createdAt = System.currentTimeMillis()
        )

        _orders.value = listOf(newOrder) + _orders.value
        clearCart()
        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(orderStatus = newStatus) else it
        }
    }

    fun updateOrderTracking(orderId: String, trackingNumber: String, courierPartner: String) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(trackingNumber = trackingNumber, courierPartner = courierPartner) else it
        }
    }

    fun findOrderByNumberAndPhone(orderNumber: String, phone: String): Order? {
        val cleanNumber = orderNumber.trim().replace("#", "").uppercase()
        val cleanPhone = phone.trim().replace("+88", "").replace(" ", "")
        return _orders.value.find {
            it.orderNumber.equals(cleanNumber, ignoreCase = true) &&
                    it.phone.replace("+88", "").replace(" ", "").endsWith(cleanPhone.takeLast(10))
        }
    }

    // Product Management (Admin)
    fun addProduct(product: Product) {
        _products.value = listOf(product) + _products.value
    }

    fun updateProduct(product: Product) {
        _products.value = _products.value.map {
            if (it.id == product.id) product else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    fun toggleProductActive(productId: String) {
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(isActive = !it.isActive) else it
        }
    }

    // Store Customization
    fun updateStoreSettings(newSettings: StoreSettings) {
        _storeSettings.value = newSettings
    }

    // Role switcher & Profile
    fun switchRole(newRole: String) {
        _userProfile.value = _userProfile.value.copy(role = newRole)
    }

    fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    // WhatsApp Message Generator
    fun buildWhatsAppOrderMessage(order: Order): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(order.createdAt))

        val itemsSummary = order.items.joinToString("\n") {
            "• ${it.product.title} (${it.selectedSize}/${it.selectedColor}) x ${it.quantity} = ৳${(it.subtotal).toInt()}"
        }

        val txInfo = if (order.transactionId.isNotBlank()) "\n*Transaction ID:* ${order.transactionId}" else ""

        val raw = """
*🛍️ NEW ORDER — SIFOX STORE*
----------------------------------------
*Order Number:* ${order.orderNumber}
*Date:* $dateStr
*Customer Name:* ${order.customerName}
*Phone:* ${order.phone} ${if (order.altPhone.isNotBlank()) "(${order.altPhone})" else ""}

*📍 Delivery Address:*
${order.fullAddress}
${order.area}, ${order.thana}, ${order.district}
*Zone:* ${order.deliveryZone.title}
${if (order.deliveryInstructions.isNotBlank()) "*Instructions:* ${order.deliveryInstructions}\n" else ""}
*📦 Ordered Products:*
$itemsSummary

----------------------------------------
*Subtotal:* ৳${order.subtotal.toInt()}
${if (order.discount > 0) "*Coupon (${order.couponCode}):* -৳${order.discount.toInt()}\n" else ""}*Delivery Fee:* ৳${order.deliveryFee.toInt()}
*Grand Total:* ৳${order.total.toInt()}
*Payment Method:* ${order.paymentMethod.title}$txInfo
*Payment Status:* ${order.paymentStatus.label}
----------------------------------------
Thank you for shopping with SiFox Store!
        """.trimIndent()

        return raw
    }

    fun getWhatsAppIntentUrl(order: Order, whatsappNumber: String): String {
        val cleanNumber = whatsappNumber.replace("+", "").replace(" ", "").replace("-", "")
        val message = buildWhatsAppOrderMessage(order)
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        return "https://wa.me/$cleanNumber?text=$encodedMessage"
    }
}
