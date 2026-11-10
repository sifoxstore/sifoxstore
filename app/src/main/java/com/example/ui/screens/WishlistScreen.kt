package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StoreRepository
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.components.ProductCard
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.RedError
import com.example.ui.theme.SkyPrimary

@Composable
fun WishlistScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val products by viewModel.products.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()

    val wishlistedProducts = products.filter { wishlistIds.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
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
                    modifier = Modifier.testTag("back_from_wishlist_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }

                Text(
                    text = "My Wishlist (${wishlistedProducts.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            }
        }

        if (wishlistedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = RedError.copy(alpha = 0.5f), modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your wishlist is empty.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Save items you love by tapping the heart icon on any product.", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary)
                    ) {
                        Text("Explore Deals", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishlistedProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductCard(
                            product = pair[0],
                            isWishlisted = true,
                            onProductClick = { p -> viewModel.navigateTo(Screen.ProductDetail(p.id)) },
                            onAddToCart = { p ->
                                StoreRepository.addToCart(p, p.sizes.firstOrNull() ?: "Standard", p.colors.firstOrNull() ?: "Default")
                                viewModel.showMessage("Added ${p.title} to Cart!")
                            },
                            onToggleWishlist = { p -> StoreRepository.toggleWishlist(p.id) },
                            modifier = Modifier.weight(1f)
                        )

                        if (pair.size > 1) {
                            ProductCard(
                                product = pair[1],
                                isWishlisted = true,
                                onProductClick = { p -> viewModel.navigateTo(Screen.ProductDetail(p.id)) },
                                onAddToCart = { p ->
                                    StoreRepository.addToCart(p, p.sizes.firstOrNull() ?: "Standard", p.colors.firstOrNull() ?: "Default")
                                    viewModel.showMessage("Added ${p.title} to Cart!")
                                },
                                onToggleWishlist = { p -> StoreRepository.toggleWishlist(p.id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
