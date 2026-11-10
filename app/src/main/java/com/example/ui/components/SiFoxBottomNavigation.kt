package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight

@Composable
fun SiFoxBottomNavigation(
    currentScreen: Screen,
    cartCount: Int,
    isAdminMode: Boolean,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
    ) {
        // 1. Home
        val isHome = currentScreen is Screen.Home
        NavigationBarItem(
            selected = isHome,
            onClick = { onNavigate(Screen.Home) },
            icon = {
                Icon(
                    imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SkyPrimary,
                selectedTextColor = SkyPrimary,
                indicatorColor = SkyPrimaryLight,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // 2. Categories
        val isCat = currentScreen is Screen.CategoryView
        NavigationBarItem(
            selected = isCat,
            onClick = { onNavigate(Screen.CategoryView("All")) },
            icon = {
                Icon(
                    imageVector = if (isCat) Icons.Filled.Category else Icons.Outlined.Category,
                    contentDescription = "Categories",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Catalog",
                    fontSize = 11.sp,
                    fontWeight = if (isCat) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SkyPrimary,
                selectedTextColor = SkyPrimary,
                indicatorColor = SkyPrimaryLight,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_item_catalog")
        )

        // 3. Cart
        val isCart = currentScreen is Screen.Cart || currentScreen is Screen.Checkout
        NavigationBarItem(
            selected = isCart,
            onClick = { onNavigate(Screen.Cart) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = SkyPrimary,
                                contentColor = Color.White
                            ) {
                                Text(text = cartCount.toString(), fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isCart) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Cart",
                    fontSize = 11.sp,
                    fontWeight = if (isCart) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SkyPrimary,
                selectedTextColor = SkyPrimary,
                indicatorColor = SkyPrimaryLight,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_item_cart")
        )

        // 4. Track / Orders
        val isOrders = currentScreen is Screen.OrdersHistory || currentScreen is Screen.OrderTracking
        NavigationBarItem(
            selected = isOrders,
            onClick = { onNavigate(Screen.OrdersHistory) },
            icon = {
                Icon(
                    imageVector = if (isOrders) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                    contentDescription = "Track Orders",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Orders",
                    fontSize = 11.sp,
                    fontWeight = if (isOrders) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SkyPrimary,
                selectedTextColor = SkyPrimary,
                indicatorColor = SkyPrimaryLight,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag("nav_item_orders")
        )

        // 5. Admin / Support
        if (isAdminMode) {
            val isAdmin = currentScreen is Screen.AdminDashboard
            NavigationBarItem(
                selected = isAdmin,
                onClick = { onNavigate(Screen.AdminDashboard) },
                icon = {
                    Icon(
                        imageVector = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                        contentDescription = "Admin",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = "Admin",
                        fontSize = 11.sp,
                        fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandBlueDark,
                    selectedTextColor = BrandBlueDark,
                    indicatorColor = Color(0xFFDBEAFE),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("nav_item_admin")
            )
        } else {
            val isSupport = currentScreen is Screen.Support
            NavigationBarItem(
                selected = isSupport,
                onClick = { onNavigate(Screen.Support) },
                icon = {
                    Icon(
                        imageVector = if (isSupport) Icons.Filled.HeadsetMic else Icons.Outlined.HeadsetMic,
                        contentDescription = "Support",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = "Support",
                        fontSize = 11.sp,
                        fontWeight = if (isSupport) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SkyPrimary,
                    selectedTextColor = SkyPrimary,
                    indicatorColor = SkyPrimaryLight,
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("nav_item_support")
            )
        }
    }
}
