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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StoreRepository
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.CategoryChips
import com.example.ui.components.ProductCard
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight

@Composable
fun CategoryProductsScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val allProducts by viewModel.products.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()

    val filteredProducts = allProducts.filter { product ->
        val matchesCategory = selectedCat == "All" || product.category.equals(selectedCat, ignoreCase = true)
        product.isActive && matchesCategory
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
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
                        modifier = Modifier.testTag("back_from_catalog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandBlueDark
                        )
                    }

                    Column {
                        Text(
                            text = "Product Departments",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        Text(
                            text = "Browse 10 Verified Categories across SiFox",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Horizontal Category Filter Tabs
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CategoryChips(
                categories = StoreRepository.categories,
                selectedCategory = selectedCat,
                onSelectCategory = { cat ->
                    viewModel.selectCategory(cat)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // If "All" selected, show department cards
        if (selectedCat == "All") {
            item {
                Text(
                    text = "Departments Overview",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(StoreRepository.categories) { category ->
                val count = allProducts.count { it.category == category.name }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.selectCategory(category.name) }
                        .testTag("dept_card_${category.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SkyPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category.name),
                                contentDescription = category.name,
                                tint = SkyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                            Text(
                                text = category.description,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "$count items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SkyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "All Department Products (${allProducts.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Product Grid
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
                        StoreRepository.addToCart(
                            p,
                            p.sizes.firstOrNull() ?: "Standard",
                            p.colors.firstOrNull() ?: "Default"
                        )
                        viewModel.showMessage("Added ${p.title} to cart!")
                    },
                    onToggleWishlist = { p ->
                        StoreRepository.toggleWishlist(p.id)
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
                            StoreRepository.addToCart(
                                p,
                                p.sizes.firstOrNull() ?: "Standard",
                                p.colors.firstOrNull() ?: "Default"
                            )
                            viewModel.showMessage("Added ${p.title} to cart!")
                        },
                        onToggleWishlist = { p ->
                            StoreRepository.toggleWishlist(p.id)
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
