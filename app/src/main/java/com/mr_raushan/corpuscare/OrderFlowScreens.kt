package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun OrderPreviewScreen(
    address: String,
    total: Int,
    items: List<Pair<Medicine, Int>>,
    onBack: () -> Unit,
    onConfirmed: (Order) -> Unit
) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }
    var showQR by remember { mutableStateOf(false) }
    var confirmedOrder by remember { mutableStateOf<Order?>(null) }

    // QR Payment Dialog
    if (showQR) {
        AlertDialog(
            onDismissRequest = { showQR = false },
            confirmButton = {
                Button(
                    onClick = {
                        // FAST CONFIRMATION (Like Appointment Flow)
                        val orderId = "ORD" + System.currentTimeMillis().toString().takeLast(6)
                        val transId = "TXN" + UUID.randomUUID().toString().take(8).uppercase()
                        val order = Order(
                            id = orderId,
                            items = items.map { OrderItem(it.first.name, it.first.price, it.second) },
                            totalAmount = total,
                            deliveryAddress = address,
                            date = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
                            transactionId = transId
                        )
                        
                        confirmedOrder = order
                        showQR = false
                        CartManager.clearCart()
                        Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_SHORT).show()

                        // Background sync
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .collection("orders").document(order.id).set(order)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) {
                    Text("Close & Get Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQR = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Scan QR to Pay", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(180.dp), tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("UPI ID: corpuscare@okaxis", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Total Amount: ₹$total", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF006766))
                    Text("Pay using any UPI App (GPay, PhonePe, Paytm)", fontSize = 12.sp, color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = if (confirmedOrder != null) onBack else onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text(
                text = if (confirmedOrder != null) "Order Receipt" else "Order Preview",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (confirmedOrder != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Text("Order Confirmed", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Order ID: ${confirmedOrder!!.id}", fontSize = 14.sp)
                        Text("TXN ID: ${confirmedOrder!!.transactionId}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF006766), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(address, fontSize = 14.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    items.forEach { (med, qty) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${med.name} x $qty", modifier = Modifier.weight(1f), fontSize = 14.sp)
                            Text(med.price, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable", fontWeight = FontWeight.Bold)
                        Text("₹$total", fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766), fontSize = 18.sp)
                    }
                }
            }
        }

        if (confirmedOrder == null) {
            Button(
                onClick = { showQR = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
            ) {
                Text("Pay Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Placeholder for download */ },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download")
                }
                Button(
                    onClick = { onConfirmed(confirmedOrder!!) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
fun OrderReceiptScreen(
    order: Order,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF006766))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Corpus Care", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Medicine Order Receipt", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
            Text("Order Confirmed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(24.dp))

            ReceiptRow("Order ID", order.id)
            ReceiptRow("Transaction ID", order.transactionId)
            ReceiptRow("Date", order.date)
            ReceiptRow("Delivery Address", order.deliveryAddress)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Items Ordered", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(item.medicineName, modifier = Modifier.weight(1f))
                    Text("${item.quantity} x ${item.price}")
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Amount Paid", fontWeight = FontWeight.Bold)
                Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF006766))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download")
            }
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1f).padding(start = 16.dp))
    }
}
