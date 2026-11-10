package com.example

import com.example.data.model.Coupon
import com.example.data.model.DeliveryZone
import com.example.data.model.PaymentMethod
import com.example.data.repository.StoreRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SiFoxStoreUnitTest {

    @Test
    fun testFixedDeliveryCharges() {
        val settings = StoreRepository.storeSettings.value
        assertEquals("Inside Dhaka delivery fee must be exactly 80.0 BDT", 80.0, settings.insideDhakaDeliveryFee, 0.001)
        assertEquals("Outside Dhaka delivery fee must be exactly 140.0 BDT", 140.0, settings.outsideDhakaDeliveryFee, 0.001)
    }

    @Test
    fun testCouponDiscountCalculations() {
        val percentCoupon = Coupon(
            code = "SIFOX10",
            discountPercent = 10,
            minSpend = 1000.0,
            description = "10% off"
        )

        // Below min spend
        assertEquals(0.0, percentCoupon.calculateDiscount(800.0), 0.001)

        // Above min spend (10% of 2000 is 200)
        assertEquals(200.0, percentCoupon.calculateDiscount(2000.0), 0.001)

        val flatCoupon = Coupon(
            code = "WELCOME500",
            flatDiscount = 500.0,
            minSpend = 3000.0,
            description = "Flat 500 off"
        )
        assertEquals(500.0, flatCoupon.calculateDiscount(3500.0), 0.001)
    }

    @Test
    fun testOrderPlacementAndTracking() {
        val order = StoreRepository.placeOrder(
            customerName = "Tanvir Hasan",
            phone = "01755554433",
            altPhone = "01888887766",
            district = "Dhaka",
            thana = "Gulshan",
            area = "Gulshan 1",
            fullAddress = "House 10, Road 5, Gulshan 1, Dhaka",
            deliveryInstructions = "Leave at reception",
            deliveryZone = DeliveryZone.INSIDE_DHAKA,
            paymentMethod = PaymentMethod.COD
        )

        assertNotNull(order.id)
        assertTrue(order.orderNumber.startsWith("SF-"))
        assertEquals(80.0, order.deliveryFee, 0.001)
        assertEquals(DeliveryZone.INSIDE_DHAKA, order.deliveryZone)

        // Verify lookup by order number and phone
        val tracked = StoreRepository.findOrderByNumberAndPhone(order.orderNumber, "01755554433")
        assertNotNull("Order must be found by order number and phone", tracked)
        assertEquals(order.id, tracked?.id)
    }

    @Test
    fun testWhatsAppMessageFormat() {
        val order = StoreRepository.placeOrder(
            customerName = "Rahim Ahmed",
            phone = "01912345678",
            altPhone = "",
            district = "Chittagong",
            thana = "Kotwali",
            area = "New Market",
            fullAddress = "Shop 12, Station Road, Chittagong",
            deliveryInstructions = "Call before arrival",
            deliveryZone = DeliveryZone.OUTSIDE_DHAKA,
            paymentMethod = PaymentMethod.BKASH,
            transactionId = "9K44X7Y2"
        )

        val message = StoreRepository.buildWhatsAppOrderMessage(order)

        assertTrue(message.contains("SIFOX STORE"))
        assertTrue(message.contains(order.orderNumber))
        assertTrue(message.contains("Rahim Ahmed"))
        assertTrue(message.contains("01912345678"))
        assertTrue(message.contains("Outside Dhaka"))
        assertTrue(message.contains("9K44X7Y2"))
        assertTrue(message.contains("Grand Total:"))
    }

    @Test
    fun testSuperAdminAuthentication() {
        val authRepo = com.example.data.repository.AdminAuthRepository

        // 1. Invalid email
        val badEmailRes = authRepo.authenticate("hacker@test.com", "Sifatvai07")
        org.junit.Assert.assertFalse("Unauthorized email must be denied", badEmailRes.first)

        // 2. Invalid password
        val badPwRes = authRepo.authenticate("mdsifatmozumder07@gmail.com", "wrongpass")
        org.junit.Assert.assertFalse("Incorrect password must be denied", badPwRes.first)

        // 3. Valid owner credentials
        val validRes = authRepo.authenticate("mdsifatmozumder07@gmail.com", "Sifatvai07")
        assertTrue("Owner credentials must be accepted", validRes.first)

        // 4. Verify 2FA OTP
        val otpRes = authRepo.verifyTwoFactorOtp("707436")
        assertTrue("Owner 2FA PIN must verify", otpRes.first)
        assertTrue("Super Admin must be marked logged in", authRepo.isSuperAdminLoggedIn.value)

        // Clean up
        authRepo.logout()
        org.junit.Assert.assertFalse("Super Admin must be logged out", authRepo.isSuperAdminLoggedIn.value)
    }

    @Test
    fun testBangladeshDivisionsAnd64Districts() {
        assertEquals("Must have exactly 8 administrative divisions", 8, StoreRepository.bangladeshDivisions.size)
        assertEquals("Must have all 64 districts accounted for", 64, StoreRepository.allBangladeshDistricts.size)

        // Dhaka inside Dhaka
        val dhakaZone = StoreRepository.calculateDeliveryZone("Dhaka")
        assertEquals(DeliveryZone.INSIDE_DHAKA, dhakaZone)
        assertEquals(80.0, dhakaZone.defaultFee, 0.001)
        assertEquals("১ দিনের মধ্যে (Within 1 Day)", dhakaZone.deliveryTime)

        // Non-Dhaka outside Dhaka
        val ctgZone = StoreRepository.calculateDeliveryZone("Chittagong")
        assertEquals(DeliveryZone.OUTSIDE_DHAKA, ctgZone)
        assertEquals(140.0, ctgZone.defaultFee, 0.001)
        assertEquals("২–৩ দিন (2–3 Days)", ctgZone.deliveryTime)

        val sylhetZone = StoreRepository.calculateDeliveryZone("Sylhet")
        assertEquals(DeliveryZone.OUTSIDE_DHAKA, sylhetZone)
        assertEquals(140.0, sylhetZone.defaultFee, 0.001)
    }

    @Test
    fun testConnectedWebsitesManagement() {
        val initialCount = StoreRepository.connectedWebsites.value.size
        assertTrue("Initial connected websites must be populated", initialCount >= 5)

        val testSite = com.example.data.model.ConnectedWebsite(
            id = "test_custom_portal",
            title = "Test Online Portal",
            url = "https://example.com/portal",
            category = "Custom Apps",
            iconEmoji = "🌐",
            isOpenInApp = true
        )

        // Add
        StoreRepository.addConnectedWebsite(testSite)
        assertTrue(StoreRepository.connectedWebsites.value.any { it.id == "test_custom_portal" })

        // Record visit
        val visitBefore = StoreRepository.connectedWebsites.value.first { it.id == "test_custom_portal" }.visitCount
        StoreRepository.recordWebsiteVisit("test_custom_portal")
        val visitAfter = StoreRepository.connectedWebsites.value.first { it.id == "test_custom_portal" }.visitCount
        assertEquals(visitBefore + 1, visitAfter)

        // Delete
        StoreRepository.deleteConnectedWebsite("test_custom_portal")
        org.junit.Assert.assertFalse(StoreRepository.connectedWebsites.value.any { it.id == "test_custom_portal" })
    }
}

