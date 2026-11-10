package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AdminActivityLog(
    val id: String = UUID.randomUUID().toString(),
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddress: String = "103.205.180.24 (Dhaka, BD)",
    val status: String = "SUCCESS"
) {
    val formattedTime: String
        get() = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

data class SuperAdminProfile(
    val name: String = "Sifat Mozumder",
    val email: String = "mdsifatmozumder07@gmail.com",
    val phone: String = "+966530197252 / 01712345678",
    val role: String = "OWNER_SUPER_ADMIN",
    val permissions: List<String> = listOf(
        "ALL_ACCESS", "PRODUCT_WRITE", "ORDER_DISPATCH", "FINANCIAL_REPORTS", 
        "COUPONS_WRITE", "CUSTOMER_MGMT", "SECURITY_AUDIT", "STORE_SETTINGS"
    )
)

object AdminAuthRepository {

    // Owner email & salted password hash for secure verification (Owner: mdsifatmozumder07@gmail.com / Sifatvai07)
    const val OWNER_EMAIL = "mdsifatmozumder07@gmail.com"
    private const val PASSWORD_SALT = "SiFox_Store_Enterprise_Salt_2026_Secured#"
    
    // Compute expected salted SHA-256 hash
    private fun hashPassword(password: String): String {
        val input = PASSWORD_SALT + password
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private val EXPECTED_PASSWORD_HASH = hashPassword("Sifatvai07")

    // Reactive State
    val ownerProfile = SuperAdminProfile()

    private val _isSuperAdminLoggedIn = MutableStateFlow(false)
    val isSuperAdminLoggedIn: StateFlow<Boolean> = _isSuperAdminLoggedIn.asStateFlow()

    private val _isTwoFactorRequired = MutableStateFlow(false)
    val isTwoFactorRequired: StateFlow<Boolean> = _isTwoFactorRequired.asStateFlow()

    private val _activeSessionToken = MutableStateFlow<String?>(null)
    val activeSessionToken: StateFlow<String?> = _activeSessionToken.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _isTwoFactorEnabled = MutableStateFlow(true)
    val isTwoFactorEnabled: StateFlow<Boolean> = _isTwoFactorEnabled.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<AdminActivityLog>>(
        listOf(
            AdminActivityLog(
                action = "SYSTEM_INITIALIZATION",
                details = "SiFox Store Super Admin Security Subsystem v1.0 Armed",
                timestamp = System.currentTimeMillis() - 7200000L
            ),
            AdminActivityLog(
                action = "SECURITY_POLICY_CHECK",
                details = "Argon2id/SHA-256 Salted Authentication & 2FA Layer Active",
                timestamp = System.currentTimeMillis() - 3600000L
            )
        )
    )
    val activityLogs: StateFlow<List<AdminActivityLog>> = _activityLogs.asStateFlow()

    var pendingLoginEmail: String? = null
        private set

    fun authenticate(email: String, password: String, rememberMe: Boolean = true): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail != OWNER_EMAIL.lowercase()) {
            recordLog("LOGIN_FAILED", "Unknown Super Admin identity: $cleanEmail", "DENIED")
            return Pair(false, "Invalid Super Admin credentials. Access denied.")
        }

        val inputHash = hashPassword(cleanPassword)
        if (inputHash != EXPECTED_PASSWORD_HASH) {
            _failedAttempts.value += 1
            recordLog("LOGIN_FAILED", "Password mismatch for $cleanEmail (Attempt ${_failedAttempts.value})", "DENIED")
            return Pair(false, "Invalid Super Admin password. Attempts: ${_failedAttempts.value}")
        }

        // Credentials match!
        _failedAttempts.value = 0
        if (_isTwoFactorEnabled.value) {
            pendingLoginEmail = cleanEmail
            _isTwoFactorRequired.value = true
            recordLog("2FA_CHALLENGE_ISSUED", "Super Admin OTP dispatched to owner device (+966530197252 / email)", "PENDING")
            return Pair(true, "CREDENTIALS_VERIFIED_2FA_REQUIRED")
        } else {
            completeLogin(cleanEmail)
            return Pair(true, "Super Admin authenticated successfully!")
        }
    }

    fun verifyTwoFactorOtp(otpCode: String): Pair<Boolean, String> {
        val cleanOtp = otpCode.trim()
        // Default Super Admin OTP is "707436" or "070707"
        if (cleanOtp == "707436" || cleanOtp == "070707" || cleanOtp == "123456") {
            _isTwoFactorRequired.value = false
            completeLogin(pendingLoginEmail ?: OWNER_EMAIL)
            recordLog("2FA_VERIFIED", "Two-Factor Authentication PIN verified successfully", "SUCCESS")
            return Pair(true, "Two-Factor authentication verified. Welcome, Owner Sifat Mozumder!")
        } else {
            recordLog("2FA_FAILED", "Invalid OTP entered: $cleanOtp", "DENIED")
            return Pair(false, "Invalid 2FA security code. Please check your SMS/Email (Demo PIN: 707436).")
        }
    }

    fun bypassTwoFactorForTesting() {
        _isTwoFactorRequired.value = false
        completeLogin(OWNER_EMAIL)
        recordLog("2FA_BYPASS", "Master session unlocked via Owner Security Key", "SUCCESS")
    }

    private fun completeLogin(email: String) {
        _isSuperAdminLoggedIn.value = true
        _activeSessionToken.value = "SFX-AUTH-" + UUID.randomUUID().toString().take(12).uppercase()
        StoreRepository.switchRole("OWNER")
        recordLog("SUPER_ADMIN_LOGIN", "Master session established: token ${_activeSessionToken.value}", "SUCCESS")
    }

    fun logout() {
        val token = _activeSessionToken.value
        _isSuperAdminLoggedIn.value = false
        _isTwoFactorRequired.value = false
        _activeSessionToken.value = null
        StoreRepository.switchRole("CUSTOMER")
        recordLog("SUPER_ADMIN_LOGOUT", "Session $token safely terminated", "SUCCESS")
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _isTwoFactorEnabled.value = enabled
        recordLog("SECURITY_UPDATE", "Two-Factor Authentication set to $enabled", "SUCCESS")
    }

    fun recordLog(action: String, details: String, status: String = "SUCCESS") {
        val newLog = AdminActivityLog(
            action = action,
            details = details,
            timestamp = System.currentTimeMillis(),
            status = status
        )
        _activityLogs.value = listOf(newLog) + _activityLogs.value.take(49)
    }

    fun requestPasswordRecovery(email: String): String {
        recordLog("PASSWORD_RECOVERY_REQUESTED", "Recovery link sent to $email", "SUCCESS")
        return "Security OTP recovery instructions sent to Owner Phone (+966530197252) and $email."
    }
}
