package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedWebsite
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyWebsitesScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val websites by viewModel.connectedWebsites.collectAsState()
    val searchQuery by viewModel.websiteSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedWebsiteCategory.collectAsState()
    val isSuperAdminLoggedIn by viewModel.isSuperAdminLoggedIn.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var websiteToEdit by remember { mutableStateOf<ConnectedWebsite?>(null) }
    var websiteToDelete by remember { mutableStateOf<ConnectedWebsite?>(null) }

    val categories = listOf("All", "E-Commerce", "Logistics", "Payments", "Government", "Cloud & Admin", "Productivity")

    // Filtered list
    val filteredWebsites = websites.filter { site ->
        val matchesCategory = selectedCategory == "All" || site.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                site.title.contains(searchQuery, ignoreCase = true) ||
                site.url.contains(searchQuery, ignoreCase = true) ||
                site.description.contains(searchQuery, ignoreCase = true) ||
                site.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    websiteToEdit = null
                    showAddDialog = true
                },
                containerColor = SkyPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_website_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Connect Website")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Header Bar
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
                        modifier = Modifier.testTag("back_from_websites_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandBlueDark
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MY WEBSITES",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlueDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SkyPrimaryLight
                            ) {
                                Text(
                                    text = "All-in-One Hub",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Central platform for all web applications & portals",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Connect button in header for quick access
                    Button(
                        onClick = {
                            websiteToEdit = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("connect_website_header_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Platform & Browser Compatibility Banner
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlueDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = SkyAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Universal Browser & Device Engine",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = "${websites.size} Connected",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Access and launch all your online services seamlessly inside SiFox Store or with any browser:",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Browser pills
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val browsers = listOf(
                                    "🌐 Chrome", "🌐 Edge", "🦊 Firefox", "🧭 Safari",
                                    "⭕ Opera", "🦁 Brave", "📱 Samsung Internet", "💻 Desktop"
                                )
                                browsers.forEach { b ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = b,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.websiteSearchQuery.value = it },
                        placeholder = { Text("Search connected websites or URL...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SkyPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.websiteSearchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = SkyPrimary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("website_search_input")
                    )
                }

                // Category Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectedWebsiteCategory.value = cat },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SkyPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = BrandBlueDark
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }

                // Websites List Count
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connected Sites (${filteredWebsites.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )

                        Text(
                            text = "Tap to open in app or browser",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Websites Cards
                if (filteredWebsites.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌐", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Websites Found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Connect your website, logistics tracking, or online portal to access it from one place.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Connect New Website")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredWebsites, key = { it.id }) { site ->
                        ConnectedWebsiteCard(
                            website = site,
                            onOpen = {
                                viewModel.recordWebsiteVisit(site.id)
                                if (site.isOpenInApp) {
                                    viewModel.navigateTo(Screen.WebViewer(site.url, site.title))
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(site.url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Could not open external browser: ${e.message}")
                                    }
                                }
                            },
                            onOpenExternal = {
                                viewModel.recordWebsiteVisit(site.id)
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(site.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    viewModel.showMessage("Could not open external browser: ${e.message}")
                                }
                            },
                            onEdit = {
                                websiteToEdit = site
                                showAddDialog = true
                            },
                            onDelete = {
                                websiteToDelete = site
                            },
                            onCopyUrl = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Website URL", site.url)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showMessage("Copied '${site.url}' to clipboard!")
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        WebsiteDialog(
            existingWebsite = websiteToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { savedWebsite ->
                if (websiteToEdit != null) {
                    viewModel.updateConnectedWebsite(savedWebsite)
                } else {
                    viewModel.addConnectedWebsite(savedWebsite)
                }
                showAddDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    websiteToDelete?.let { site ->
        AlertDialog(
            onDismissRequest = { websiteToDelete = null },
            title = { Text("Remove Connected Website?") },
            text = { Text("Are you sure you want to remove '${site.title}' from My Websites?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteConnectedWebsite(site.id)
                        websiteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { websiteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ConnectedWebsiteCard(
    website: ConnectedWebsite,
    onOpen: () -> Unit,
    onOpenExternal: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyUrl: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("website_card_${website.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SkyPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = website.iconEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = website.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = website.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = website.url,
                        fontSize = 11.sp,
                        color = SkyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Three dots menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("website_options_${website.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open in App", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                onOpen()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Open in Chrome / Browser", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                onOpenExternal()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Copy URL", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                onCopyUrl()
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        DropdownMenuItem(
                            text = { Text("Edit Details", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Remove Website", fontSize = 12.sp, color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (website.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = website.description,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row with stats and open buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${website.visitCount} visits",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (website.isOpenInApp) Color(0xFFECFDF5) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = if (website.isOpenInApp) "In-App Viewer" else "External Browser",
                            fontSize = 9.sp,
                            color = if (website.isOpenInApp) Color(0xFF047857) else Color(0xFF1D4ED8),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onOpenExternal,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Browser", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WebsiteDialog(
    existingWebsite: ConnectedWebsite?,
    onDismiss: () -> Unit,
    onSave: (ConnectedWebsite) -> Unit
) {
    var title by remember { mutableStateOf(existingWebsite?.title ?: "") }
    var url by remember { mutableStateOf(existingWebsite?.url ?: "https://") }
    var category by remember { mutableStateOf(existingWebsite?.category ?: "E-Commerce") }
    var iconEmoji by remember { mutableStateOf(existingWebsite?.iconEmoji ?: "🌐") }
    var description by remember { mutableStateOf(existingWebsite?.description ?: "") }
    var isOpenInApp by remember { mutableStateOf(existingWebsite?.isOpenInApp ?: true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val emojis = listOf("🌐", "🦊", "🛒", "🚚", "📦", "💳", "⚡", "📮", "⚙️", "📁", "📊", "🔒")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingWebsite != null) "Edit Connected Website" else "Connect New Website",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlueDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Website Title *") },
                    placeholder = { Text("e.g. My Online Store") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL (HTTPS) *") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category *") },
                    placeholder = { Text("E-Commerce, Logistics, Payments...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Emoji Picker Row
                Text("Select Icon Emoji:", fontSize = 11.sp, color = Color(0xFF64748B))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        val isSelected = iconEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SkyPrimaryLight else Color(0xFFF1F5F9))
                                .clickable { iconEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Brief notes about this web portal") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Open in App Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Open Inside App Viewer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Toggle off to launch directly in Chrome / system browser", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = isOpenInApp,
                        onCheckedChange = { isOpenInApp = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SkyPrimary, checkedTrackColor = SkyPrimaryLight)
                    )
                }

                errorMsg?.let { err ->
                    Text(text = err, color = Color(0xFFEF4444), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || url.isBlank()) {
                        errorMsg = "Please provide both title and URL."
                        return@Button
                    }
                    var cleanUrl = url.trim()
                    if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                        cleanUrl = "https://$cleanUrl"
                    }

                    val website = ConnectedWebsite(
                        id = existingWebsite?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        url = cleanUrl,
                        category = if (category.isBlank()) "General" else category.trim(),
                        iconEmoji = iconEmoji,
                        description = description.trim(),
                        isOpenInApp = isOpenInApp,
                        isPublic = true,
                        visitCount = existingWebsite?.visitCount ?: 0,
                        createdAt = existingWebsite?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(website)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (existingWebsite != null) "Update" else "Connect Website")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
