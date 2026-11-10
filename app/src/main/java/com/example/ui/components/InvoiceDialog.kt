package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Order
import com.example.data.repository.StoreRepository
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.SkyPrimaryLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = sdf.format(Date(order.createdAt))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("invoice_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Fox Logo & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_sifox_logo_trimmed),
                                contentDescription = "Logo",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SIFOX STORE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandBlueDark
                            )
                            Text(
                                text = "TAX INVOICE / CASH MEMO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyPrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                // Invoice Meta & Customer Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "BILLED TO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(text = order.customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlueDark)
                        Text(text = order.phone, fontSize = 12.sp, color = Color(0xFF334155))
                        if (order.altPhone.isNotBlank()) {
                            Text(text = "Alt: ${order.altPhone}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Text(
                            text = "${order.fullAddress}, ${order.area}, ${order.thana}, ${order.district}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "INVOICE #", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(text = order.orderNumber, fontSize = 15.sp, fontWeight = FontWeight.Black, color = SkyPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "DATE:", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = dateStr, fontSize = 11.sp, color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "STATUS: ${order.orderStatus.label.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Itemized Table Header
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ITEM / VARIANT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                        Text(text = "QTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(0.7f))
                        Text(text = "TOTAL (৳)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                    }
                }

                // Items list
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(text = item.product.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandBlueDark)
                            Text(
                                text = "Size: ${item.selectedSize} • Color: ${item.selectedColor}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(text = "x${item.quantity}", fontSize = 12.sp, color = Color(0xFF334155), modifier = Modifier.weight(0.7f))
                        Text(
                            text = "৳${item.subtotal.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calculations Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Subtotal", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(text = "৳${order.subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    if (order.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Coupon Discount (${order.couponCode ?: ""})", fontSize = 12.sp, color = GreenSuccess)
                            Text(text = "-৳${order.discount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Delivery Fee (${order.deliveryZone.title})", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(text = "৳${order.deliveryFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    HorizontalDivider(color = Color(0xFFCBD5E1))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Grand Total Payable", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = BrandBlueDark)
                        Text(text = "৳${order.total.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SkyPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment & Tracking Details
                Surface(
                    color = SkyPrimaryLight.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Payment Method: ${order.paymentMethod.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlueDark
                        )
                        if (order.transactionId.isNotBlank()) {
                            Text(
                                text = "Transaction ID: ${order.transactionId} (${order.paymentStatus.label})",
                                fontSize = 11.sp,
                                color = Color(0xFF0369A1)
                            )
                        }
                        if (order.trackingNumber.isNotBlank()) {
                            Text(
                                text = "Courier: ${order.courierPartner} • Tracking #${order.trackingNumber}",
                                fontSize = 11.sp,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "SiFox Store Invoice #${order.orderNumber}")
                                putExtra(Intent.EXTRA_TEXT, StoreRepository.buildWhatsAppOrderMessage(order))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val intentUrl = StoreRepository.getWhatsAppIntentUrl(order, "+966530197252")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(intentUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WhatsApp", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
