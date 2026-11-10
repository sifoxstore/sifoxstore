package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight
import com.example.ui.theme.TextPrimaryDark

@Composable
fun SiFoxHeader(
    viewModel: StoreViewModel,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    cartCount: Int,
    role: String,
    onCartClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Row: Logo, Branding, WhatsApp, Admin Toggle, Cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand with Fox Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.Home) }
                        .testTag("brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sifox_logo_trimmed),
                            contentDescription = "SiFox Official Logo",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SiFox",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlueDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STORE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyPrimary
                            )
                        }
                        Text(
                            text = "BD Official E-Commerce",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: WhatsApp, Role Switcher, Cart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick WhatsApp Support icon
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/966530197252?text=Hello%20SiFox%20Store%20Support,%20I%20have%20an%20inquiry.")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("whatsapp_quick_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "WhatsApp Support",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Role Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (role == "ADMIN" || role == "OWNER") Color(0xFFEFF6FF) else SkyPrimaryLight,
                        modifier = Modifier
                            .clickable { viewModel.toggleRole() }
                            .testTag("role_switcher_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (role == "ADMIN" || role == "OWNER") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = "Role Mode",
                                tint = if (role == "ADMIN" || role == "OWNER") Color(0xFF1D4ED8) else SkyPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (role == "ADMIN" || role == "OWNER") "Admin" else "Buyer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (role == "ADMIN" || role == "OWNER") Color(0xFF1D4ED8) else SkyPrimary
                            )
                        }
                    }

                    // Cart Icon with Badge
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.testTag("header_cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = SkyPrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = cartCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Shopping Cart",
                                tint = BrandBlueDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Three Dots Overflow Menu (Requirement 1: Top Right Corner Three Dots Menu)
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("top_right_three_dots_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = BrandBlueDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(220.dp)
                        ) {
                            // SUPER ADMIN ACCESS - Prominent option
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "SUPER ADMIN ACCESS",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1D4ED8)
                                            )
                                            Text(
                                                text = "Owner Portal",
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    if (com.example.data.repository.AdminAuthRepository.isSuperAdminLoggedIn.value) {
                                        viewModel.navigateTo(Screen.SuperAdminDashboard)
                                    } else {
                                        viewModel.navigateTo(Screen.SuperAdminLogin)
                                    }
                                },
                                modifier = Modifier
                                    .background(Color(0xFFEFF6FF))
                                    .testTag("super_admin_access_menu_item")
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // MY WEBSITES - Universal Platform Hub
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = SkyPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "MY WEBSITES",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandBlueDark
                                            )
                                            Text(
                                                text = "All-in-One Browser Hub",
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.navigateTo(Screen.MyWebsites)
                                },
                                modifier = Modifier.testTag("my_websites_menu_item")
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = SkyPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Track Shipment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.navigateTo(Screen.OrderTracking)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("WhatsApp Support", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/966530197252?text=Hello%20SiFox%20Store%20Support,%20I%20have%20an%20inquiry.")
                                    )
                                    context.startActivity(intent)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SupportAgent,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Help & Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.navigateTo(Screen.Support)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Search products, gadgets, fashion in BD...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SkyPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    disabledContainerColor = Color(0xFFF8FAFC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("search_input_field")
            )
        }
    }
}
