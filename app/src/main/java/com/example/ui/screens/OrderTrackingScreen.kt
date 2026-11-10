package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.OrderStatus
import com.example.ui.Screen
import com.example.ui.StoreViewModel
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight

@Composable
fun OrderTrackingScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val orderNumberInput by viewModel.trackingOrderNumberInput.collectAsState()
    val phoneInput by viewModel.trackingPhoneInput.collectAsState()
    val trackedOrder by viewModel.trackedOrderResult.collectAsState()
    val trackingError by viewModel.trackingError.collectAsState()

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
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier.testTag("back_from_tracking_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }

                Text(
                    text = "Shipment & Order Tracking",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lookup Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Track Your Parcel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    Text(
                        text = "Enter your Order Number and Mobile Number to check real-time courier status.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = orderNumberInput,
                        onValueChange = { viewModel.trackingOrderNumberInput.value = it.uppercase() },
                        label = { Text("Order Number (e.g. SF-74291)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracking_order_number_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { viewModel.trackingPhoneInput.value = it },
                        label = { Text("Customer Mobile Number") },
                        placeholder = { Text("01XXXXXXXXX") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracking_phone_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.performTrackingLookup() },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_track_order_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Order Status", fontWeight = FontWeight.Bold)
                    }

                    if (trackingError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = trackingError!!,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Tracking Result Details
            if (trackedOrder != null) {
                val order = trackedOrder!!
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Order #${order.orderNumber}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandBlueDark
                                )
                                Text(
                                    text = "Courier: ${order.courierPartner}",
                                    fontSize = 12.sp,
                                    color = SkyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = GreenSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = order.orderStatus.label.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GreenSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (order.trackingNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tracking Code: ${order.trackingNumber}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step Timeline (Pending → Confirmed → Processing → Shipped → Delivered)
                        val steps = listOf(
                            Pair("Order Placed", "Your order has been recorded in SiFox Store."),
                            Pair("Confirmed", "Customer and address verified by seller."),
                            Pair("Processing", "Items packaged and inspected in Dhaka Hub."),
                            Pair("Shipped", "Dispatched with ${order.courierPartner}."),
                            Pair("Delivered", "Parcel delivered safely to customer.")
                        )

                        val currentStep = order.orderStatus.stepIndex

                        steps.forEachIndexed { index, step ->
                            val isCompleted = index <= currentStep
                            val isCurrent = index == currentStep

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Indicator circle & vertical line
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCompleted) SkyPrimary else Color(0xFFE2E8F0)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    if (index < steps.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(34.dp)
                                                .background(
                                                    if (index < currentStep) SkyPrimary else Color(0xFFE2E8F0)
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 16.dp else 0.dp)) {
                                    Text(
                                        text = step.first,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isCompleted) BrandBlueDark else Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = step.second,
                                        fontSize = 11.sp,
                                        color = if (isCompleted) Color(0xFF64748B) else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer & Shipping Address summary
                        Text(
                            text = "Destination: ${order.fullAddress}, ${order.thana}, ${order.district}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Total Payable: ৳${order.total.toInt()} (${order.paymentMethod.title})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.openInvoice(order) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyPrimaryLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = SkyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Tax Invoice", color = SkyPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
