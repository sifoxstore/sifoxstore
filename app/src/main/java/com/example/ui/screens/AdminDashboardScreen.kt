package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.data.repository.StoreRepository
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
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
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Orders (${orders.size})", "Products (${products.size})", "Coupons", "Store Settings")

    // Modals state
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddCouponDialog by remember { mutableStateOf(false) }

    // Calculated metrics
    val totalRevenue = orders.filter { it.orderStatus != OrderStatus.CANCELLED }.sumOf { it.total }
    val pendingCount = orders.count { it.orderStatus == OrderStatus.PENDING || it.orderStatus == OrderStatus.CONFIRMED }
    val deliveredCount = orders.count { it.orderStatus == OrderStatus.DELIVERED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier.testTag("admin_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = SkyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "SiFox Owner Admin",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandBlueDark
                        )
                        Text(
                            text = "Live Store Management & Dispatch Hub",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Switch to Customer View button
                OutlinedButton(
                    onClick = {
                        viewModel.toggleRole()
                        viewModel.navigateTo(Screen.Home)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("switch_to_buyer_btn")
                ) {
                    Text("Exit Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tab Row
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
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) SkyPrimary else Color(0xFF64748B)
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> AdminOverviewTab(
                totalRevenue = totalRevenue,
                totalOrders = orders.size,
                pendingOrders = pendingCount,
                deliveredOrders = deliveredCount,
                totalProducts = products.size,
                recentOrders = orders.take(3),
                onViewOrder = { viewModel.openInvoice(it) },
                onGoOrders = { selectedTab = 1 },
                onAddProduct = { showAddProductDialog = true }
            )
            1 -> AdminOrdersTab(
                orders = orders,
                onUpdateStatus = { orderId, newStatus ->
                    StoreRepository.updateOrderStatus(orderId, newStatus)
                    viewModel.showMessage("Order status updated to ${newStatus.label}")
                },
                onUpdateTracking = { orderId, tracking, courier ->
                    StoreRepository.updateOrderTracking(orderId, tracking, courier)
                    viewModel.showMessage("Tracking updated: $tracking ($courier)")
                },
                onViewInvoice = { viewModel.openInvoice(it) }
            )
            2 -> AdminProductsTab(
                products = products,
                onAddProduct = { showAddProductDialog = true },
                onEditProduct = { editingProduct = it },
                onDeleteProduct = {
                    StoreRepository.deleteProduct(it.id)
                    viewModel.showMessage("Deleted ${it.title}")
                },
                onToggleActive = {
                    StoreRepository.toggleProductActive(it.id)
                    viewModel.showMessage("Toggled active status for ${it.title}")
                }
            )
            3 -> AdminCouponsTab(
                coupons = coupons,
                onAddCoupon = { showAddCouponDialog = true },
                onDeleteCoupon = {
                    StoreRepository.deleteCoupon(it)
                    viewModel.showMessage("Coupon $it removed.")
                }
            )
            4 -> AdminSettingsTab(
                storeSettings = storeSettings,
                onSave = { updated ->
                    StoreRepository.updateStoreSettings(updated)
                    viewModel.showMessage("Store settings saved successfully!")
                }
            )
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog || editingProduct != null) {
        val prodToEdit = editingProduct
        AddOrEditProductDialog(
            product = prodToEdit,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSave = { newOrUpdated ->
                if (prodToEdit != null) {
                    StoreRepository.updateProduct(newOrUpdated)
                    viewModel.showMessage("Updated ${newOrUpdated.title}")
                } else {
                    StoreRepository.addProduct(newOrUpdated)
                    viewModel.showMessage("Added ${newOrUpdated.title} to store!")
                }
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    // Add Coupon Dialog
    if (showAddCouponDialog) {
        AddCouponDialog(
            onDismiss = { showAddCouponDialog = false },
            onSave = { coupon ->
                StoreRepository.addCoupon(coupon)
                viewModel.showMessage("Coupon ${coupon.code} added successfully!")
                showAddCouponDialog = false
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    totalRevenue: Double,
    totalOrders: Int,
    pendingOrders: Int,
    deliveredOrders: Int,
    totalProducts: Int,
    recentOrders: List<Order>,
    onViewOrder: (Order) -> Unit,
    onGoOrders: () -> Unit,
    onAddProduct: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onGoOrders,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Process Orders", fontSize = 12.sp)
                }
            }
        }

        // 4 KPI Cards (matches the user's dashboard inspiration screenshot!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Sales",
                    value = "৳${totalRevenue.toInt()}",
                    growth = "+14.8%",
                    icon = Icons.Default.AttachMoney,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = GreenSuccess,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Total Orders",
                    value = totalOrders.toString(),
                    growth = "+8.3%",
                    icon = Icons.Default.ShoppingCart,
                    iconBg = SkyPrimaryLight,
                    iconTint = SkyPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Pending Delivery",
                    value = pendingOrders.toString(),
                    growth = "In Hub",
                    icon = Icons.Default.LocalShipping,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = AmberWarning,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Catalog Products",
                    value = totalProducts.toString(),
                    growth = "10 Depts",
                    icon = Icons.Default.Inventory2,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Orders Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Incoming Orders",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )

                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyPrimary,
                    modifier = Modifier.clickable { onGoOrders() }
                )
            }
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
fun MetricCard(
    title: String,
    value: String,
    growth: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = BrandBlueDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = growth,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersTab(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    onUpdateTracking: (String, String, String) -> Unit,
    onViewInvoice: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(orders) { order ->
            var trackingNumberInput by remember { mutableStateOf(order.trackingNumber) }
            var courierInput by remember { mutableStateOf(order.courierPartner) }
            var statusDropdownExpanded by remember { mutableStateOf(false) }

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
                                text = "${order.customerName} • ${order.phone}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        // Order Status Dropdown
                        ExposedDropdownMenuBox(
                            expanded = statusDropdownExpanded,
                            onExpandedChange = { statusDropdownExpanded = it }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SkyPrimaryLight,
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .clickable { statusDropdownExpanded = true }
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
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded)
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false }
                            ) {
                                OrderStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.label) },
                                        onClick = {
                                            onUpdateStatus(order.id, status)
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Address: ${order.fullAddress}, ${order.area}, ${order.thana}, ${order.district}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Text(
                        text = "Payment: ${order.paymentMethod.title} • Total: ৳${order.total.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )

                    if (order.transactionId.isNotBlank()) {
                        Text(
                            text = "TrxID: ${order.transactionId} (${order.paymentStatus.label})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tracking input row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = trackingNumberInput,
                            onValueChange = { trackingNumberInput = it },
                            placeholder = { Text("Courier Tracking #", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )

                        Button(
                            onClick = { onUpdateTracking(order.id, trackingNumberInput, courierInput) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text("Update", fontSize = 11.sp)
                        }

                        IconButton(onClick = { onViewInvoice(order) }) {
                            Icon(Icons.Default.Receipt, contentDescription = "Invoice", tint = SkyPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsTab(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_add_new_product_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Product to SiFox Catalog", fontWeight = FontWeight.Bold)
            }
        }

        items(products) { product ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyPrimary
                            )
                            if (product.isFeatured) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "FEATURED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = product.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark,
                            maxLines = 1
                        )

                        Text(
                            text = "Price: ৳${product.effectivePrice.toInt()} • Stock: ${product.stock} units",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = product.isActive,
                            onCheckedChange = { onToggleActive(product) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SkyPrimary)
                        )

                        IconButton(onClick = { onEditProduct(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyPrimary)
                        }

                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCouponsTab(
    coupons: List<Coupon>,
    onAddCoupon: () -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddCoupon,
                colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_add_coupon_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create New Discount Coupon", fontWeight = FontWeight.Bold)
            }
        }

        items(coupons) { coupon ->
            Card(
                shape = RoundedCornerShape(12.dp),
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = SkyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = coupon.code,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlueDark
                            )
                        }
                        Text(
                            text = coupon.description,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Min Spend: ৳${coupon.minSpend.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SkyPrimary
                        )
                    }

                    IconButton(onClick = { onDeleteCoupon(coupon.code) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedError)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    storeSettings: StoreSettings,
    onSave: (StoreSettings) -> Unit
) {
    var storeName by remember { mutableStateOf(storeSettings.storeName) }
    var whatsappNumber by remember { mutableStateOf(storeSettings.whatsappNumber) }
    var email by remember { mutableStateOf(storeSettings.supportEmail) }
    var insideDhakaFee by remember { mutableStateOf(storeSettings.insideDhakaDeliveryFee.toInt().toString()) }
    var outsideDhakaFee by remember { mutableStateOf(storeSettings.outsideDhakaDeliveryFee.toInt().toString()) }
    var announcement by remember { mutableStateOf(storeSettings.announcementText) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Store Customization & Delivery Rates",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    Text(
                        text = "Update branding, contact lines, and nationwide delivery rules.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("Customer Support WhatsApp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Support Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = insideDhakaFee,
                            onValueChange = { insideDhakaFee = it },
                            label = { Text("Inside Dhaka (৳)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = outsideDhakaFee,
                            onValueChange = { outsideDhakaFee = it },
                            label = { Text("Outside Dhaka (৳)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = announcement,
                        onValueChange = { announcement = it },
                        label = { Text("Top Announcement Ticker") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val inside = insideDhakaFee.toDoubleOrNull() ?: 80.0
                            val outside = outsideDhakaFee.toDoubleOrNull() ?: 140.0
                            onSave(
                                storeSettings.copy(
                                    storeName = storeName.trim(),
                                    whatsappNumber = whatsappNumber.trim(),
                                    supportEmail = email.trim(),
                                    insideDhakaDeliveryFee = inside,
                                    outsideDhakaDeliveryFee = outside,
                                    announcementText = announcement.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Store Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Gadgets & Electronics") }
    var price by remember { mutableStateOf(product?.price?.toInt()?.toString() ?: "1500") }
    var discountPrice by remember { mutableStateOf(product?.discountPrice?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "20") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80") }
    var isFeatured by remember { mutableStateOf(product?.isFeatured ?: false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product != null) "Edit Product" else "Add New Product", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        StoreRepository.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (৳)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = discountPrice,
                        onValueChange = { discountPrice = it },
                        label = { Text("Sale Price") },
                        placeholder = { Text("Optional") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock Qty") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Featured", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    }
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 1000.0
                    val dp = discountPrice.toDoubleOrNull()
                    val s = stock.toIntOrNull() ?: 20
                    val prod = Product(
                        id = product?.id ?: UUID.randomUUID().toString(),
                        title = title.ifBlank { "New SiFox Item" },
                        category = category,
                        price = p,
                        discountPrice = if (dp != null && dp > 0) dp else null,
                        stock = s,
                        description = description,
                        imageUrl = imageUrl,
                        isFeatured = isFeatured,
                        isActive = product?.isActive ?: true
                    )
                    onSave(prod)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCouponDialog(
    onDismiss: () -> Unit,
    onSave: (Coupon) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var percent by remember { mutableStateOf("10") }
    var minSpend by remember { mutableStateOf("1000") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Discount Coupon", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code (e.g. SIFOX25)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = percent,
                    onValueChange = { percent = it },
                    label = { Text("Discount Percentage (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minSpend,
                    onValueChange = { minSpend = it },
                    label = { Text("Minimum Spend (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    placeholder = { Text("10% off for all BD buyers") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        val coupon = Coupon(
                            code = code.trim(),
                            discountPercent = percent.toIntOrNull() ?: 10,
                            minSpend = minSpend.toDoubleOrNull() ?: 0.0,
                            description = desc.ifBlank { "$percent% discount" }
                        )
                        onSave(coupon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary)
            ) {
                Text("Add Coupon")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
