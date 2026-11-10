package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.InvoiceDialog
import com.example.ui.components.SiFoxBottomNavigation
import com.example.ui.components.SiFoxHeader
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoryProductsScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InAppWebViewerScreen
import com.example.ui.screens.MyWebsitesScreen
import com.example.ui.screens.OrderSuccessScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OrdersHistoryScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SuperAdminDashboardScreen
import com.example.ui.screens.SuperAdminLoginScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SiFoxStoreApp()
            }
        }
    }
}

@Composable
fun SiFoxStoreApp(
    viewModel: StoreViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val selectedInvoiceOrder by viewModel.selectedInvoiceOrder.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val isAdminMode = userProfile.role == "ADMIN" || userProfile.role == "OWNER"

    // Show top search header only on browsing screens
    val showHeader = (currentScreen is Screen.Home ||
            currentScreen is Screen.CategoryView ||
            currentScreen is Screen.Wishlist) &&
            currentScreen !is Screen.SuperAdminLogin &&
            currentScreen !is Screen.SuperAdminDashboard &&
            currentScreen !is Screen.MyWebsites &&
            currentScreen !is Screen.WebViewer

    // Show bottom navigation on primary destinations
    val showBottomNav = currentScreen !is Screen.Checkout &&
            currentScreen !is Screen.OrderSuccess &&
            currentScreen !is Screen.SuperAdminLogin &&
            currentScreen !is Screen.SuperAdminDashboard &&
            currentScreen !is Screen.WebViewer

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showHeader) {
                SiFoxHeader(
                    viewModel = viewModel,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.onSearchQueryChanged(it) },
                    cartCount = cartCount,
                    role = userProfile.role,
                    onCartClick = { viewModel.navigateTo(Screen.Cart) },
                    onAdminClick = { viewModel.navigateTo(Screen.AdminDashboard) }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                SiFoxBottomNavigation(
                    currentScreen = currentScreen,
                    cartCount = cartCount,
                    isAdminMode = isAdminMode,
                    onNavigate = { dest -> viewModel.navigateTo(dest) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(viewModel = viewModel)
                is Screen.CategoryView -> CategoryProductsScreen(viewModel = viewModel)
                is Screen.ProductDetail -> ProductDetailScreen(productId = screen.productId, viewModel = viewModel)
                is Screen.Cart -> CartScreen(viewModel = viewModel)
                is Screen.Checkout -> CheckoutScreen(viewModel = viewModel)
                is Screen.OrderSuccess -> OrderSuccessScreen(orderId = screen.orderId, viewModel = viewModel)
                is Screen.OrderTracking -> OrderTrackingScreen(viewModel = viewModel)
                is Screen.OrdersHistory -> OrdersHistoryScreen(viewModel = viewModel)
                is Screen.Wishlist -> WishlistScreen(viewModel = viewModel)
                is Screen.MyWebsites -> MyWebsitesScreen(viewModel = viewModel)
                is Screen.WebViewer -> InAppWebViewerScreen(url = screen.url, title = screen.title, viewModel = viewModel)
                is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                is Screen.SuperAdminLogin -> SuperAdminLoginScreen(viewModel = viewModel)
                is Screen.SuperAdminDashboard -> SuperAdminDashboardScreen(viewModel = viewModel)
                is Screen.Support -> SupportScreen(viewModel = viewModel)
            }

            // Global Invoice Dialog
            selectedInvoiceOrder?.let { order ->
                InvoiceDialog(
                    order = order,
                    onDismiss = { viewModel.closeInvoice() }
                )
            }
        }
    }
}
