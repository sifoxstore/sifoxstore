package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.CategoryChips
import com.example.ui.components.ProductCard
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandSkySurface
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight

@Composable
fun HomeScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProducts by viewModel.products.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()

    val filteredProducts = allProducts.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.title.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCat == "All" || product.category.equals(selectedCat, ignoreCase = true)
        product.isActive && matchesQuery && matchesCategory
    }

    val featuredProducts = allProducts.filter { it.isFeatured && it.isActive }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Announcement ticker
            item {
                Surface(
                    color = SkyPrimaryLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📢",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = storeSettings.announcementText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlueDark,
                            maxLines = 1
                        )
                    }
                }
            }

            // Promotional Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("hero_promo_banner_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_promo_banner_1794279118145),
                            contentDescription = "Summer Mega Sale",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle dark gradient for clear text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.65f),
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = SkyAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SPECIAL DHAKA & BD OFFER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandBlueDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Summer Mega Sale",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Text(
                                text = "Up to 50% Off on Top Tech & Fashion",
                                fontSize = 12.sp,
                                color = Color(0xFFE0F2FE),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.selectCategory("Special Offers") },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("banner_shop_now_btn")
                            ) {
                                Text(
                                    text = "Shop Now",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Trust & Delivery Highlights (Fixed charges ৳80 inside Dhaka / ৳140 outside)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dhaka delivery badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Delivery",
                                tint = SkyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Dhaka ৳80",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Text(
                                    text = "Outside ৳140",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Payment badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Payment",
                                tint = Color(0xFFE2136E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "bKash & Nagad",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Text(
                                    text = "+ Cash on Delivery",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // MY WEBSITES — Universal Platform Hub Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandBlueDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.navigateTo(Screen.MyWebsites) }
                        .testTag("home_my_websites_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MY WEBSITES",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SkyAccent
                                ) {
                                    Text(
                                        text = "Universal Hub",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlueDark,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Connect & launch all online services, tracking, and websites",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1),
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(Screen.MyWebsites) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Categories Header & Chips
            item {
                Column(modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Categories",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        Text(
                            text = "View All (10)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyPrimary,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.CategoryView("All")) }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CategoryChips(
                        categories = com.example.data.repository.StoreRepository.categories,
                        selectedCategory = selectedCat,
                        onSelectCategory = { cat ->
                            viewModel.selectCategory(cat)
                        }
                    )
                }
            }

            // Featured Carousel if on "All"
            if (selectedCat == "All" && searchQuery.isBlank()) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥 Featured Deals",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "LIMITED STOCK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredProducts) { product ->
                                ProductCard(
                                    product = product,
                                    isWishlisted = wishlistIds.contains(product.id),
                                    onProductClick = { p ->
                                        viewModel.navigateTo(Screen.ProductDetail(p.id))
                                    },
                                    onAddToCart = { p ->
                                        com.example.data.repository.StoreRepository.addToCart(
                                            p,
                                            p.sizes.firstOrNull() ?: "Standard",
                                            p.colors.firstOrNull() ?: "Default"
                                        )
                                        viewModel.showMessage("Added ${p.title} to cart!")
                                    },
                                    onToggleWishlist = { p ->
                                        com.example.data.repository.StoreRepository.toggleWishlist(p.id)
                                    },
                                    modifier = Modifier.width(220.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Best Selling / Catalog Grid Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCat == "All") "Best Selling Products" else "$selectedCat (${filteredProducts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    Text(
                        text = "${filteredProducts.size} items",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Products in 2-column layout
            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No products found matching your search.",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.selectCategory("All")
                        }) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                items(filteredProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductCard(
                            product = pair[0],
                            isWishlisted = wishlistIds.contains(pair[0].id),
                            onProductClick = { p ->
                                viewModel.navigateTo(Screen.ProductDetail(p.id))
                            },
                            onAddToCart = { p ->
                                com.example.data.repository.StoreRepository.addToCart(
                                    p,
                                    p.sizes.firstOrNull() ?: "Standard",
                                    p.colors.firstOrNull() ?: "Default"
                                )
                                viewModel.showMessage("Added ${p.title} to cart!")
                            },
                            onToggleWishlist = { p ->
                                com.example.data.repository.StoreRepository.toggleWishlist(p.id)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (pair.size > 1) {
                            ProductCard(
                                product = pair[1],
                                isWishlisted = wishlistIds.contains(pair[1].id),
                                onProductClick = { p ->
                                    viewModel.navigateTo(Screen.ProductDetail(p.id))
                                },
                                onAddToCart = { p ->
                                    com.example.data.repository.StoreRepository.addToCart(
                                        p,
                                        p.sizes.firstOrNull() ?: "Standard",
                                        p.colors.firstOrNull() ?: "Default"
                                    )
                                    viewModel.showMessage("Added ${p.title} to cart!")
                                },
                                onToggleWishlist = { p ->
                                    com.example.data.repository.StoreRepository.toggleWishlist(p.id)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // WhatsApp Floating Button
        FloatingActionButton(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/966530197252?text=Hello%20SiFox%20Store%20Support,%20I%20want%20to%20order%20products.")
                )
                context.startActivity(intent)
            },
            containerColor = Color(0xFF25D366),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
                .testTag("whatsapp_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "WhatsApp Order Support",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
