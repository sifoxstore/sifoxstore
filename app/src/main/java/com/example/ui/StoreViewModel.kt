package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DeliveryZone
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class CategoryView(val categoryName: String) : Screen()
    data class ProductDetail(val productId: String) : Screen()
    object Cart : Screen()
    object Checkout : Screen()
    data class OrderSuccess(val orderId: String) : Screen()
    object OrderTracking : Screen()
    object OrdersHistory : Screen()
    object Wishlist : Screen()
    object MyWebsites : Screen()
    data class WebViewer(val url: String, val title: String) : Screen()
    object SuperAdminLogin : Screen()
    object SuperAdminDashboard : Screen()
    object AdminDashboard : Screen()
    object Support : Screen()
}

class StoreViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _selectedInvoiceOrder = MutableStateFlow<Order?>(null)
    val selectedInvoiceOrder: StateFlow<Order?> = _selectedInvoiceOrder.asStateFlow()

    // Checkout Form State
    val selectedCountry = MutableStateFlow("Bangladesh")
    val selectedDivision = MutableStateFlow("Dhaka")
    val fullName = MutableStateFlow("Sifat Mozumder")
    val mobileNumber = MutableStateFlow("01712345678")
    val altNumber = MutableStateFlow("01887654321")
    val selectedDistrict = MutableStateFlow("Dhaka")
    val thana = MutableStateFlow("Dhanmondi")
    val area = MutableStateFlow("Road 27")
    val fullAddress = MutableStateFlow("House 42, Road 27, Dhanmondi, Dhaka 1209")
    val deliveryInstructions = MutableStateFlow("Please call before delivering")
    val deliveryZone = MutableStateFlow(DeliveryZone.INSIDE_DHAKA)
    val paymentMethod = MutableStateFlow(PaymentMethod.COD)
    val transactionId = MutableStateFlow("")

    // Bangladesh geography helpers
    val bangladeshDivisions = StoreRepository.bangladeshDivisions
    val bangladeshDistrictsByDivision = StoreRepository.bangladeshDistrictsByDivision
    val allBangladeshDistricts = StoreRepository.allBangladeshDistricts

    // Tracking Lookup State
    val trackingOrderNumberInput = MutableStateFlow("")
    val trackingPhoneInput = MutableStateFlow("")
    val trackedOrderResult = MutableStateFlow<Order?>(null)
    val trackingError = MutableStateFlow<String?>(null)

    // Data streams from repository
    val products = StoreRepository.products
    val cartItems = StoreRepository.cartItems
    val wishlistIds = StoreRepository.wishlistIds
    val coupons = StoreRepository.coupons
    val appliedCoupon = StoreRepository.appliedCoupon
    val storeSettings = StoreRepository.storeSettings
    val orders = StoreRepository.orders
    val userProfile = StoreRepository.userProfile
    val connectedWebsites = StoreRepository.connectedWebsites

    // Universal Website Integration Hub State
    val websiteSearchQuery = MutableStateFlow("")
    val selectedWebsiteCategory = MutableStateFlow("All")

    val cartCount: StateFlow<Int> = cartItems.combine(cartItems) { items, _ ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = cartItems.combine(cartItems) { items, _ ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun openInvoice(order: Order) {
        _selectedInvoiceOrder.value = order
    }

    fun closeInvoice() {
        _selectedInvoiceOrder.value = null
    }

    fun onDivisionChanged(newDivision: String) {
        selectedDivision.value = newDivision
        val districtsInDivision = bangladeshDistrictsByDivision[newDivision] ?: emptyList()
        if (selectedDistrict.value !in districtsInDivision && districtsInDivision.isNotEmpty()) {
            onDistrictChanged(districtsInDivision.first())
        }
    }

    fun onDistrictChanged(newDistrict: String) {
        selectedDistrict.value = newDistrict
        // Auto-assign fixed delivery zone: Inside Dhaka ৳80 vs Outside Dhaka ৳140
        deliveryZone.value = StoreRepository.calculateDeliveryZone(newDistrict)
    }

    fun applyCouponCode(code: String) {
        val result = StoreRepository.applyCoupon(code, cartSubtotal.value)
        showMessage(result.second)
    }

    fun removeCoupon() {
        StoreRepository.removeCoupon()
        showMessage("Coupon removed.")
    }

    fun performCheckout(): Order? {
        if (fullName.value.isBlank() || mobileNumber.value.isBlank() || fullAddress.value.isBlank()) {
            showMessage("Please fill in recipient name, phone, and delivery address.")
            return null
        }
        if (paymentMethod.value != PaymentMethod.COD && transactionId.value.isBlank()) {
            showMessage("Please enter the ${paymentMethod.value.title} transaction ID.")
            return null
        }

        val order = StoreRepository.placeOrder(
            customerName = fullName.value.trim(),
            phone = mobileNumber.value.trim(),
            altPhone = altNumber.value.trim(),
            country = selectedCountry.value.trim(),
            division = selectedDivision.value.trim(),
            district = selectedDistrict.value.trim(),
            thana = thana.value.trim(),
            area = area.value.trim(),
            fullAddress = fullAddress.value.trim(),
            deliveryInstructions = deliveryInstructions.value.trim(),
            deliveryZone = deliveryZone.value,
            paymentMethod = paymentMethod.value,
            transactionId = transactionId.value.trim()
        )

        // Reset transactionId
        transactionId.value = ""
        navigateTo(Screen.OrderSuccess(order.id))
        return order
    }

    // Universal Website Hub actions
    fun addConnectedWebsite(website: com.example.data.model.ConnectedWebsite) {
        StoreRepository.addConnectedWebsite(website)
        showMessage("Website '${website.title}' connected successfully!")
    }

    fun updateConnectedWebsite(website: com.example.data.model.ConnectedWebsite) {
        StoreRepository.updateConnectedWebsite(website)
        showMessage("Website '${website.title}' updated.")
    }

    fun deleteConnectedWebsite(id: String) {
        StoreRepository.deleteConnectedWebsite(id)
        showMessage("Website removed from My Websites.")
    }

    fun recordWebsiteVisit(id: String) {
        StoreRepository.recordWebsiteVisit(id)
    }

    fun performTrackingLookup() {
        trackingError.value = null
        if (trackingOrderNumberInput.value.isBlank() || trackingPhoneInput.value.isBlank()) {
            trackingError.value = "Please enter both Order Number (e.g. SF-74291) and Phone number."
            trackedOrderResult.value = null
            return
        }
        val found = StoreRepository.findOrderByNumberAndPhone(
            trackingOrderNumberInput.value,
            trackingPhoneInput.value
        )
        if (found != null) {
            trackedOrderResult.value = found
            trackingError.value = null
        } else {
            trackedOrderResult.value = null
            trackingError.value = "No order found matching #${trackingOrderNumberInput.value.trim()} with phone ${trackingPhoneInput.value.trim()}."
        }
    }

    fun toggleRole() {
        val currentRole = userProfile.value.role
        val nextRole = if (currentRole == "ADMIN" || currentRole == "OWNER") "CUSTOMER" else "ADMIN"
        StoreRepository.switchRole(nextRole)
        showMessage("Switched to $nextRole mode.")
    }

    // Super Admin Authentication State
    val adminEmail = MutableStateFlow("mdsifatmozumder07@gmail.com")
    val adminPassword = MutableStateFlow("")
    val adminPasswordVisible = MutableStateFlow(false)
    val adminRememberMe = MutableStateFlow(true)
    val adminOtpInput = MutableStateFlow("")
    val adminAuthError = MutableStateFlow<String?>(null)

    val isSuperAdminLoggedIn = com.example.data.repository.AdminAuthRepository.isSuperAdminLoggedIn
    val isTwoFactorRequired = com.example.data.repository.AdminAuthRepository.isTwoFactorRequired
    val activityLogs = com.example.data.repository.AdminAuthRepository.activityLogs
    val isTwoFactorEnabled = com.example.data.repository.AdminAuthRepository.isTwoFactorEnabled
    val activeSessionToken = com.example.data.repository.AdminAuthRepository.activeSessionToken

    fun submitSuperAdminLogin(): Boolean {
        adminAuthError.value = null
        val result = com.example.data.repository.AdminAuthRepository.authenticate(
            email = adminEmail.value,
            password = adminPassword.value,
            rememberMe = adminRememberMe.value
        )
        if (result.first) {
            if (result.second == "CREDENTIALS_VERIFIED_2FA_REQUIRED") {
                // 2FA modal/step active
                return true
            } else {
                adminPassword.value = ""
                navigateTo(Screen.SuperAdminDashboard)
                showMessage("Welcome, Super Admin Sifat Mozumder!")
                return true
            }
        } else {
            adminAuthError.value = result.second
            return false
        }
    }

    fun submitAdminTwoFactor(): Boolean {
        adminAuthError.value = null
        val result = com.example.data.repository.AdminAuthRepository.verifyTwoFactorOtp(adminOtpInput.value)
        if (result.first) {
            adminOtpInput.value = ""
            adminPassword.value = ""
            navigateTo(Screen.SuperAdminDashboard)
            showMessage(result.second)
            return true
        } else {
            adminAuthError.value = result.second
            return false
        }
    }

    fun logoutSuperAdmin() {
        com.example.data.repository.AdminAuthRepository.logout()
        adminPassword.value = ""
        adminOtpInput.value = ""
        navigateTo(Screen.Home)
        showMessage("Super Admin session securely terminated.")
    }

    fun requestAdminPasswordRecovery() {
        val msg = com.example.data.repository.AdminAuthRepository.requestPasswordRecovery(adminEmail.value)
        showMessage(msg)
    }
}
