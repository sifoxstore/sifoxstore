package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Coupon
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.repository.AdminAuthRepository
import com.example.data.repository.StoreRepository
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val coupons by viewModel.coupons.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val sessionToken by viewModel.activeSessionToken.collectAsState()
    val is2FAActive by viewModel.isTwoFactorEnabled.collectAsState()
    val connectedWebsites by viewModel.connectedWebsites.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Overview & Revenue",
        "Dispatches (${orders.size})",
        "Catalog (${products.size})",
        "My Websites (${connectedWebsites.size})",
        "Shipment Tracking",
        "Customers",
        "Payments & bKash",
        "Coupons",
        "Security & 2FA",
        "Store Settings"
    )

    // Calculated KPI metrics
    val totalRevenue = orders.filter { it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.total }
    val pendingCount = orders.count { it.orderStatus == OrderStatus.PENDING || it.orderStatus == OrderStatus.CONFIRMED }
    val processingCount = orders.count { it.orderStatus == OrderStatus.PROCESSING }
    val shippedCount = orders.count { it.orderStatus == OrderStatus.SHIPPED }
    val deliveredCount = orders.count { it.orderStatus == OrderStatus.DELIVERED }
    val cancelledCount = orders.count { it.orderStatus == OrderStatus.CANCELLED }

    val bkashRevenue = orders.filter { it.paymentMethod == PaymentMethod.BKASH && it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.total }
    val nagadRevenue = orders.filter { it.paymentMethod == PaymentMethod.NAGAD && it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.total }
    val codRevenue = orders.filter { it.paymentMethod == PaymentMethod.COD && it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.total }

    // Dialogs state
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddCouponDialog by remember { mutableStateOf(false) }
    var showAddWebsiteDialog by remember { mutableStateOf(false) }
    var editingWebsite by remember { mutableStateOf<com.example.data.model.ConnectedWebsite?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Super Admin Top Header (Enterprise Branding)
        Surface(
            color = Color(0xFF0F172A),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SkyAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SiFox Super Admin",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFF97316),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "OWNER ACCESS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Admin: ${AdminAuthRepository.ownerProfile.name} • ${AdminAuthRepository.OWNER_EMAIL}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Logout & Store return action
                    Button(
                        onClick = { viewModel.logoutSuperAdmin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("super_admin_logout_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Security & Session Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session: ${sessionToken ?: "SFX-AUTH-ACTIVE"} • 2FA: ${if (is2FAActive) "Active" else "Disabled"}",
                        fontSize = 9.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Switch to Customer View ↗",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Home) }
                    )
                }
            }
        }

        // Horizontal Module Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) SkyPrimary else Color(0xFF64748B)
                        )
                    }
                )
            }
        }

        // Tab Content Switching
        when (selectedTab) {
            0 -> SuperAdminOverviewModule(
                totalRevenue = totalRevenue,
                totalOrders = orders.size,
                pendingCount = pendingCount,
                processingCount = processingCount,
                shippedCount = shippedCount,
                deliveredCount = deliveredCount,
                cancelledCount = cancelledCount,
                bkashRevenue = bkashRevenue,
                nagadRevenue = nagadRevenue,
                codRevenue = codRevenue,
                totalProducts = products.size,
                recentOrders = orders.take(5),
                onViewOrder = { viewModel.openInvoice(it) }
            )
            1 -> SuperAdminDispatchesModule(
                orders = orders,
                onUpdateStatus = { orderId, newStatus ->
                    StoreRepository.updateOrderStatus(orderId, newStatus)
                    AdminAuthRepository.recordLog("ORDER_STATUS_CHANGE", "Order $orderId status changed to ${newStatus.label}")
                    viewModel.showMessage("Order status updated to ${newStatus.label}")
                },
                onViewInvoice = { viewModel.openInvoice(it) }
            )
            2 -> SuperAdminCatalogModule(
                products = products,
                onAddProduct = { showAddProductDialog = true },
                onEditProduct = { editingProduct = it },
                onDeleteProduct = {
                    StoreRepository.deleteProduct(it.id)
                    AdminAuthRepository.recordLog("PRODUCT_DELETED", "Deleted product: ${it.title}")
                    viewModel.showMessage("Deleted ${it.title}")
                },
                onToggleActive = {
                    StoreRepository.toggleProductActive(it.id)
                    AdminAuthRepository.recordLog("PRODUCT_STATUS_TOGGLE", "Toggled status for: ${it.title}")
                    viewModel.showMessage("Toggled active status for ${it.title}")
                }
            )
            3 -> SuperAdminWebsitesModule(
                websites = connectedWebsites,
                onAddWebsite = {
                    editingWebsite = null
                    showAddWebsiteDialog = true
                },
                onEditWebsite = {
                    editingWebsite = it
                    showAddWebsiteDialog = true
                },
                onDeleteWebsite = {
                    StoreRepository.deleteConnectedWebsite(it.id)
                    AdminAuthRepository.recordLog("WEBSITE_DELETED", "Removed website: ${it.title}")
                    viewModel.showMessage("Removed ${it.title}")
                },
                onToggleInApp = { site ->
                    val updated = site.copy(isOpenInApp = !site.isOpenInApp)
                    StoreRepository.updateConnectedWebsite(updated)
                    AdminAuthRepository.recordLog("WEBSITE_CONFIG_UPDATE", "Toggled in-app viewer for ${site.title}")
                },
                onTogglePublic = { site ->
                    val updated = site.copy(isPublic = !site.isPublic)
                    StoreRepository.updateConnectedWebsite(updated)
                    AdminAuthRepository.recordLog("WEBSITE_VISIBILITY_UPDATE", "Toggled visibility for ${site.title}")
                },
                onLaunch = { site ->
                    viewModel.recordWebsiteVisit(site.id)
                    viewModel.navigateTo(Screen.WebViewer(site.url, site.title))
                }
            )
            4 -> SuperAdminShipmentTrackingModule(
                orders = orders,
                onUpdateTracking = { orderId, tracking, courier ->
                    StoreRepository.updateOrderTracking(orderId, tracking, courier)
                    AdminAuthRepository.recordLog("TRACKING_UPDATED", "Order $orderId -> $tracking via $courier")
                    viewModel.showMessage("Tracking code $tracking saved!")
                }
            )
            5 -> SuperAdminCustomersModule(orders = orders)
            6 -> SuperAdminPaymentsModule(
                orders = orders,
                bkashTotal = bkashRevenue,
                nagadTotal = nagadRevenue,
                codTotal = codRevenue
            )
            7 -> SuperAdminCouponsModule(
                coupons = coupons,
                onAddCoupon = { showAddCouponDialog = true },
                onDeleteCoupon = {
                    StoreRepository.deleteCoupon(it)
                    AdminAuthRepository.recordLog("COUPON_REMOVED", "Removed coupon code: $it")
                    viewModel.showMessage("Coupon $it removed.")
                }
            )
            8 -> SuperAdminSecurityModule(
                activityLogs = activityLogs,
                is2FAActive = is2FAActive,
                onToggle2FA = { AdminAuthRepository.toggleTwoFactor(it) }
            )
            9 -> SuperAdminSettingsModule(
                storeSettings = storeSettings,
                onSave = { updated ->
                    StoreRepository.updateStoreSettings(updated)
                    AdminAuthRepository.recordLog("STORE_SETTINGS_UPDATE", "Owner updated delivery rates & contact info")
                    viewModel.showMessage("Store settings updated successfully!")
                }
            )
        }
    }

    // Product Add/Edit Dialog
    if (showAddProductDialog || editingProduct != null) {
        val prod = editingProduct
        AddOrEditProductDialog(
            product = prod,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSave = { newProd ->
                if (prod != null) {
                    StoreRepository.updateProduct(newProd)
                    AdminAuthRepository.recordLog("PRODUCT_UPDATED", "Updated ${newProd.title}")
                    viewModel.showMessage("Updated ${newProd.title}")
                } else {
                    StoreRepository.addProduct(newProd)
                    AdminAuthRepository.recordLog("PRODUCT_ADDED", "Created new product: ${newProd.title}")
                    viewModel.showMessage("Added ${newProd.title} to SiFox Store!")
                }
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    // Coupon Add Dialog
    if (showAddCouponDialog) {
        AddCouponDialog(
            onDismiss = { showAddCouponDialog = false },
            onSave = { c ->
                StoreRepository.addCoupon(c)
                AdminAuthRepository.recordLog("COUPON_CREATED", "Created coupon: ${c.code}")
                viewModel.showMessage("Coupon ${c.code} activated!")
                showAddCouponDialog = false
            }
        )
    }

    // Website Add/Edit Dialog
    if (showAddWebsiteDialog || editingWebsite != null) {
        val site = editingWebsite
        WebsiteDialog(
            existingWebsite = site,
            onDismiss = {
                showAddWebsiteDialog = false
                editingWebsite = null
            },
            onSave = { saved ->
                if (site != null) {
                    StoreRepository.updateConnectedWebsite(saved)
                    AdminAuthRepository.recordLog("WEBSITE_UPDATED", "Updated ${saved.title}")
                    viewModel.showMessage("Updated ${saved.title}")
                } else {
                    StoreRepository.addConnectedWebsite(saved)
                    AdminAuthRepository.recordLog("WEBSITE_CONNECTED", "Connected new website: ${saved.title}")
                    viewModel.showMessage("Connected ${saved.title}")
                }
                showAddWebsiteDialog = false
                editingWebsite = null
            }
        )
    }
}

// -------------------------------------------------------------
// MODULE 0: OVERVIEW & REVENUE REPORTS
// -------------------------------------------------------------
@Composable
fun SuperAdminOverviewModule(
    totalRevenue: Double,
    totalOrders: Int,
    pendingCount: Int,
    processingCount: Int,
    shippedCount: Int,
    deliveredCount: Int,
    cancelledCount: Int,
    bkashRevenue: Double,
    nagadRevenue: Double,
    codRevenue: Double,
    totalProducts: Int,
    recentOrders: List<Order>,
    onViewOrder: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Level Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Gross Sales",
                    value = "৳${totalRevenue.toInt()}",
                    growth = "+14.8% MoM",
                    icon = Icons.Default.AttachMoney,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = GreenSuccess,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Total Orders",
                    value = totalOrders.toString(),
                    growth = "All Time",
                    icon = Icons.Default.ShoppingCart,
                    iconBg = SkyPrimaryLight,
                    iconTint = SkyPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Status Breakdown Grid
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Order Pipeline Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PipelinePill("Pending", pendingCount, AmberWarning, Color(0xFFFEF3C7))
                        PipelinePill("Processing", processingCount, Color(0xFF0284C7), SkyPrimaryLight)
                        PipelinePill("Shipped", shippedCount, Color(0xFF7C3AED), Color(0xFFEDE9FE))
                        PipelinePill("Delivered", deliveredCount, GreenSuccess, Color(0xFFDCFCE7))
                        PipelinePill("Cancelled", cancelledCount, RedError, Color(0xFFFEE2E2))
                    }
                }
            }
        }

        // Revenue Breakdown by Channel
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Payment Gateway Collections",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFDF2F8),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("bKash Merchant", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2136E))
                                Text("৳${bkashRevenue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF9D174D))
                            }
                        }

                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Nagad Merchant", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                Text("৳${nagadRevenue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF9A3412))
                            }
                        }

                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Cash On Delivery", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                Text("৳${codRevenue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF166534))
                            }
                        }
                    }
                }
            }
        }

        // Recent Orders Header
        item {
            Text(
                text = "Recent Incoming Dispatches",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlueDark
            )
        }

        items(recentOrders) { order ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewOrder(order) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Order #${order.orderNumber} • ${order.customerName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        Text(
                            text = "${order.deliveryZone.title} • ${order.paymentMethod.title}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "৳${order.total.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = SkyPrimary
                        )
                        Text(
                            text = order.orderStatus.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.orderStatus) {
                                OrderStatus.DELIVERED -> GreenSuccess
                                OrderStatus.SHIPPED -> SkyPrimary
                                else -> AmberWarning
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PipelinePill(label: String, count: Int, textColor: Color, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(text = count.toString(), fontSize = 13.sp, fontWeight = FontWeight.Black, color = textColor)
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

// -------------------------------------------------------------
// MODULE 1: DISPATCHES & ORDER MANAGEMENT
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDispatchesModule(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    onViewInvoice: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order #${order.orderNumber}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandBlueDark
                            )
                            Text(
                                text = "${order.customerName} (${order.phone})",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        // Status dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SkyPrimaryLight,
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .clickable { expanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order.orderStatus.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyPrimary
                                    )
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                OrderStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.label) },
                                        onClick = {
                                            onUpdateStatus(order.id, st)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Destination: ${order.fullAddress}, ${order.area}, ${order.thana}, ${order.district}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "৳${order.total.toInt()} (${order.paymentMethod.title})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )

                        OutlinedButton(
                            onClick = { onViewInvoice(order) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tax Invoice", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 2: CATALOG & PRODUCT MANAGEMENT
// -------------------------------------------------------------
@Composable
fun SuperAdminCatalogModule(
    products: List<Product>,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onToggleActive: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Item to Catalog", fontWeight = FontWeight.Bold)
            }
        }

        items(products) { p ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = p.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyPrimary
                        )
                        Text(
                            text = p.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark,
                            maxLines = 1
                        )
                        Text(
                            text = "৳${p.effectivePrice.toInt()} • Stock: ${p.stock} units",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = p.isActive,
                            onCheckedChange = { onToggleActive(p) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SkyPrimary)
                        )
                        IconButton(onClick = { onEditProduct(p) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyPrimary)
                        }
                        IconButton(onClick = { onDeleteProduct(p) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedError)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 3: SHIPMENT TRACKING HUB (Section 6)
// -------------------------------------------------------------
@Composable
fun SuperAdminShipmentTrackingModule(
    orders: List<Order>,
    onUpdateTracking: (String, String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(orders) { order ->
            var trackingCode by remember { mutableStateOf(order.trackingNumber) }
            var courier by remember { mutableStateOf(order.courierPartner) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Parcel #${order.orderNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                            Text(text = "Recipient: ${order.customerName} • ${order.phone}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Surface(
                            color = GreenSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = order.orderStatus.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = courier,
                            onValueChange = { courier = it },
                            label = { Text("Courier (Steadfast/Pathao)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = trackingCode,
                            onValueChange = { trackingCode = it },
                            label = { Text("Tracking Code #", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onUpdateTracking(order.id, trackingCode, courier) },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Shipment & Dispatch Tracking", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 4: CUSTOMERS MANAGEMENT
// -------------------------------------------------------------
@Composable
fun SuperAdminCustomersModule(orders: List<Order>) {
    val uniqueCustomers = orders.distinctBy { it.phone }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Registered Customers Directory (${uniqueCustomers.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlueDark
            )
        }

        items(uniqueCustomers) { c ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = c.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                        Text(text = "District: ${c.district}", fontSize = 11.sp, color = SkyPrimary, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "Phone: ${c.phone} ${if (c.altPhone.isNotBlank()) "• Alt: ${c.altPhone}" else ""}", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = "Delivery Address: ${c.fullAddress}, ${c.thana}", fontSize = 11.sp, color = Color(0xFF334155))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 5: PAYMENTS & BKASH / NAGAD RECONCILIATIONS
// -------------------------------------------------------------
@Composable
fun SuperAdminPaymentsModule(
    orders: List<Order>,
    bkashTotal: Double,
    nagadTotal: Double,
    codTotal: Double
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Payment Audit Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• bKash Verified: ৳${bkashTotal.toInt()} (Merchant 01700-SIFOX)\n• Nagad Verified: ৳${nagadTotal.toInt()} (Merchant 01800-SIFOX)\n• COD Outstanding: ৳${codTotal.toInt()}", fontSize = 12.sp, color = Color(0xFF475569))
                }
            }
        }

        item {
            Text("Transaction Log & Verification", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
        }

        items(orders) { o ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Order #${o.orderNumber} • ${o.paymentMethod.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                        if (o.transactionId.isNotBlank()) {
                            Text(text = "TrxID: ${o.transactionId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        }
                        Text(text = "Status: ${o.paymentStatus.label}", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Text(text = "৳${o.total.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = SkyPrimary)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 6: COUPONS
// -------------------------------------------------------------
@Composable
fun SuperAdminCouponsModule(
    coupons: List<Coupon>,
    onAddCoupon: () -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    AdminCouponsTab(
        coupons = coupons,
        onAddCoupon = onAddCoupon,
        onDeleteCoupon = onDeleteCoupon
    )
}

// -------------------------------------------------------------
// MODULE 7: SECURITY SYSTEM & AUDIT LOGS (Section 7)
// -------------------------------------------------------------
@Composable
fun SuperAdminSecurityModule(
    activityLogs: List<com.example.data.repository.AdminActivityLog>,
    is2FAActive: Boolean,
    onToggle2FA: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 2FA Security Control Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SkyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Two-Factor Authentication (2FA)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                        }
                        Text("Require 6-digit OTP confirmation on Owner Super Admin login.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }

                    Switch(
                        checked = is2FAActive,
                        onCheckedChange = onToggle2FA,
                        colors = SwitchDefaults.colors(checkedThumbColor = SkyPrimary)
                    )
                }
            }
        }

        // Security Audit Logs
        item {
            Text("Real-Time Security Activity Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
        }

        items(activityLogs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = log.action, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                        Surface(
                            color = if (log.status == "SUCCESS") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = log.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (log.status == "SUCCESS") Color(0xFF166534) else RedError,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = log.details, fontSize = 11.sp, color = Color(0xFF475569))
                    Text(text = "${log.formattedTime} • IP: ${log.ipAddress}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODULE 8: STORE SETTINGS
// -------------------------------------------------------------
@Composable
fun SuperAdminSettingsModule(
    storeSettings: StoreSettings,
    onSave: (StoreSettings) -> Unit
) {
    AdminSettingsTab(
        storeSettings = storeSettings,
        onSave = onSave
    )
}

// -------------------------------------------------------------
// MODULE: CONNECTED WEBSITES & BROWSER HUB MANAGER
// -------------------------------------------------------------
@Composable
fun SuperAdminWebsitesModule(
    websites: List<com.example.data.model.ConnectedWebsite>,
    onAddWebsite: () -> Unit,
    onEditWebsite: (com.example.data.model.ConnectedWebsite) -> Unit,
    onDeleteWebsite: (com.example.data.model.ConnectedWebsite) -> Unit,
    onToggleInApp: (com.example.data.model.ConnectedWebsite) -> Unit,
    onTogglePublic: (com.example.data.model.ConnectedWebsite) -> Unit,
    onLaunch: (com.example.data.model.ConnectedWebsite) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connected Websites Manager",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Control in-app viewer, browser visibility & links for the universal hub",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = onAddWebsite,
                            colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Website", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(websites, key = { it.id }) { site ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SkyPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(site.iconEmoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = site.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = site.category,
                                        fontSize = 9.sp,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = site.url,
                                fontSize = 11.sp,
                                color = SkyPrimary,
                                maxLines = 1
                            )
                        }

                        IconButton(onClick = { onLaunch(site) }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Launch", tint = SkyPrimary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onEditWebsite(site) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDeleteWebsite(site) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    // Controls row: In-App toggle & Public visibility toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("In-App WebView:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = site.isOpenInApp,
                                onCheckedChange = { onToggleInApp(site) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SkyPrimary, checkedTrackColor = SkyPrimaryLight)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Public in Hub:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = site.isPublic,
                                onCheckedChange = { onTogglePublic(site) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981), checkedTrackColor = Color(0xFFD1FAE5))
                            )
                        }

                        Text(
                            text = "${site.visitCount} visits",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

