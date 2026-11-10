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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryZone
import com.example.data.model.PaymentMethod
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.BkashPink
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Cart)
    }

    val cartItems by viewModel.cartItems.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedDivision by viewModel.selectedDivision.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val altNumber by viewModel.altNumber.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val thana by viewModel.thana.collectAsState()
    val area by viewModel.area.collectAsState()
    val fullAddress by viewModel.fullAddress.collectAsState()
    val deliveryInstructions by viewModel.deliveryInstructions.collectAsState()
    val deliveryZone by viewModel.deliveryZone.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val transactionId by viewModel.transactionId.collectAsState()

    val subtotal = cartItems.sumOf { it.subtotal }
    val discount = appliedCoupon?.calculateDiscount(subtotal) ?: 0.0
    val deliveryFee = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) {
        storeSettings.insideDhakaDeliveryFee
    } else {
        storeSettings.outsideDhakaDeliveryFee
    }
    val total = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)

    val divisions = viewModel.bangladeshDivisions
    val districtsForDivision = viewModel.bangladeshDistrictsByDivision[selectedDivision] ?: viewModel.allBangladeshDistricts

    var divisionDropdownExpanded by remember { mutableStateOf(false) }
    var districtDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Header
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
                    onClick = { viewModel.navigateTo(Screen.Cart) },
                    modifier = Modifier.testTag("back_from_checkout_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }

                Text(
                    text = "Shipping & Checkout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer Info Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SkyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. Customer Information",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { viewModel.fullName.value = it },
                            label = { Text("Full Name *") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = { viewModel.mobileNumber.value = it },
                                label = { Text("Mobile Number *") },
                                placeholder = { Text("01XXXXXXXXX") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checkout_phone_input")
                            )

                            OutlinedTextField(
                                value = altNumber,
                                onValueChange = { viewModel.altNumber.value = it },
                                label = { Text("Alternative Phone") },
                                placeholder = { Text("Optional") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checkout_alt_phone_input")
                            )
                        }
                    }
                }
            }

            // Delivery Address & Zone
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. Delivery Address (BD)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Country (Fixed to Bangladesh)
                        OutlinedTextField(
                            value = selectedCountry,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Country *") },
                            leadingIcon = { Text("🇧🇩", fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_country_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Division & District in a responsive row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Division Dropdown
                            ExposedDropdownMenuBox(
                                expanded = divisionDropdownExpanded,
                                onExpandedChange = { divisionDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedDivision,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Division *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divisionDropdownExpanded) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("division_selector")
                                )
                                ExposedDropdownMenu(
                                    expanded = divisionDropdownExpanded,
                                    onDismissRequest = { divisionDropdownExpanded = false }
                                ) {
                                    divisions.forEach { div ->
                                        DropdownMenuItem(
                                            text = { Text(div) },
                                            onClick = {
                                                viewModel.onDivisionChanged(div)
                                                divisionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 2. District Dropdown (All 64 districts grouped by division)
                            ExposedDropdownMenuBox(
                                expanded = districtDropdownExpanded,
                                onExpandedChange = { districtDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedDistrict,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("District (64) *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtDropdownExpanded) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("district_selector")
                                )
                                ExposedDropdownMenu(
                                    expanded = districtDropdownExpanded,
                                    onDismissRequest = { districtDropdownExpanded = false }
                                ) {
                                    districtsForDivision.forEach { dist ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(dist)
                                                    if (dist.equals("Dhaka", ignoreCase = true)) {
                                                        Text("৳80 • 1 Day", fontSize = 10.sp, color = SkyPrimary, fontWeight = FontWeight.Bold)
                                                    } else {
                                                        Text("৳140 • 2-3 Days", fontSize = 10.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.onDistrictChanged(dist)
                                                districtDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Real-Time Auto-Calculated Delivery Zone & Time Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) SkyPrimaryLight else Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (deliveryZone == DeliveryZone.INSIDE_DHAKA) SkyPrimary.copy(alpha = 0.3f) else Color(0xFFF59E0B).copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) SkyPrimary else Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (deliveryZone == DeliveryZone.INSIDE_DHAKA)
                                                "Inside Dhaka City: ৳80"
                                            else
                                                "Outside Dhaka City: ৳140",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) BrandBlueDark else Color(0xFF92400E)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) SkyPrimary else Color(0xFFD97706)
                                    ) {
                                        Text(
                                            text = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) "১ দিনের মধ্যে (24h)" else "২–৩ দিন (48-72h)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (deliveryZone == DeliveryZone.INSIDE_DHAKA)
                                        "⚡ Superfast Express Delivery via Pathao / Steadfast within 24 hours"
                                    else
                                        "🚚 Reliable Home Delivery via Steadfast Courier across 63 districts within 2-3 business days",
                                    fontSize = 10.sp,
                                    color = if (deliveryZone == DeliveryZone.INSIDE_DHAKA) Color(0xFF0369A1) else Color(0xFF78350F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = thana,
                                onValueChange = { viewModel.thana.value = it },
                                label = { Text("Thana / Upazila *") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checkout_thana_input")
                            )

                            OutlinedTextField(
                                value = area,
                                onValueChange = { viewModel.area.value = it },
                                label = { Text("Area / Sector *") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checkout_area_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fullAddress,
                            onValueChange = { viewModel.fullAddress.value = it },
                            label = { Text("Full Street & House Address *") },
                            placeholder = { Text("House 12, Road 4, Block C...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_address_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = deliveryInstructions,
                            onValueChange = { viewModel.deliveryInstructions.value = it },
                            label = { Text("Delivery Instructions") },
                            placeholder = { Text("e.g. Call before delivery / After 4 PM") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Payment Methods Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = SkyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3. Select Payment Method",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. Cash on Delivery
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.paymentMethod.value = PaymentMethod.COD }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == PaymentMethod.COD,
                                onClick = { viewModel.paymentMethod.value = PaymentMethod.COD },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyPrimary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cash on Delivery (COD)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Text(
                                    text = "Pay cash when product arrives at your doorstep",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 2. bKash Merchant
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.paymentMethod.value = PaymentMethod.BKASH }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == PaymentMethod.BKASH,
                                onClick = { viewModel.paymentMethod.value = PaymentMethod.BKASH },
                                colors = RadioButtonDefaults.colors(selectedColor = BkashPink)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "bKash Merchant Payment",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BkashPink
                                )
                                Text(
                                    text = "Merchant Number: 01700-SIFOX (01700743690)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 3. Nagad Merchant
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.paymentMethod.value = PaymentMethod.NAGAD }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = paymentMethod == PaymentMethod.NAGAD,
                                onClick = { viewModel.paymentMethod.value = PaymentMethod.NAGAD },
                                colors = RadioButtonDefaults.colors(selectedColor = NagadOrange)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nagad Merchant Payment",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NagadOrange
                                )
                                Text(
                                    text = "Merchant Number: 01800-SIFOX (01800743690)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Transaction ID field if bKash or Nagad
                        if (paymentMethod != PaymentMethod.COD) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (paymentMethod == PaymentMethod.BKASH) Color(0xFFFDF2F8) else Color(0xFFFFF7ED),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${paymentMethod.title} Instructions:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlueDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "1. Send ৳${total.toInt()} to official Merchant Number\n2. Enter your 8-10 digit Transaction ID (TrxID) below to verify:",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = transactionId,
                                        onValueChange = { viewModel.transactionId.value = it.uppercase() },
                                        label = { Text("Transaction ID (TrxID) *") },
                                        placeholder = { Text("e.g. 9K88F4A2") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("transaction_id_input")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Payable Amount Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal (${cartItems.sumOf { it.quantity }} items)", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("৳${subtotal.toInt()}", fontSize = 12.sp)
                        }

                        if (discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount (${appliedCoupon?.code})", fontSize = 12.sp, color = GreenSuccess)
                                Text("-৳${discount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee (${deliveryZone.title})", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("৳${deliveryFee.toInt()}", fontSize = 12.sp)
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                            Text("৳${total.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SkyPrimary)
                        }
                    }
                }
            }
        }

        // Bottom Place Order Button
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { viewModel.performCheckout() },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_order_btn")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm & Place Order (৳${total.toInt()})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔒 Guaranteed 100% Secure Checkout • WhatsApp Order confirmation sync",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
