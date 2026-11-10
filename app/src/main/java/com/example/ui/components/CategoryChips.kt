package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight

fun getCategoryIcon(name: String): ImageVector {
    return when (name) {
        "Men's Fashion" -> Icons.Default.Checkroom
        "Women's Fashion" -> Icons.Default.AutoAwesome
        "Gadgets & Electronics" -> Icons.Default.Devices
        "Lifestyle Products" -> Icons.Default.Spa
        "Accessories" -> Icons.Default.Watch
        "Beauty & Personal Care" -> Icons.Default.Face
        "Home & Living" -> Icons.Default.Home
        "Sports & Fitness" -> Icons.Default.FitnessCenter
        "Kids & Baby Products" -> Icons.Default.ChildCare
        "Special Offers" -> Icons.Default.LocalOffer
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryChips(
    categories: List<Category>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // "All" item
        item {
            val isSelected = selectedCategory == "All"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) SkyPrimary else Color.White,
                shadowElevation = if (isSelected) 3.dp else 1.dp,
                modifier = Modifier
                    .clickable { onSelectCategory("All") }
                    .testTag("category_chip_All")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "All",
                        tint = if (isSelected) Color.White else SkyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "All Categories",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else BrandBlueDark
                    )
                }
            }
        }

        items(categories) { category ->
            val isSelected = selectedCategory == category.name
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) SkyPrimary else Color.White,
                shadowElevation = if (isSelected) 3.dp else 1.dp,
                modifier = Modifier
                    .clickable { onSelectCategory(category.name) }
                    .testTag("category_chip_${category.name.replace(" ", "_")}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.name),
                        contentDescription = category.name,
                        tint = if (isSelected) Color.White else SkyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else BrandBlueDark
                    )
                }
            }
        }
    }
}
