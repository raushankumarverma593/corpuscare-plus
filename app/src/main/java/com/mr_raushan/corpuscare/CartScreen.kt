package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CartScreen(
    onBack: () -> Unit,
    userAddress: String = "123, MG Road, Indore",
    savedAddresses: List<UserAddress> = emptyList(),
    onAddNewAddress: (String) -> Unit = {},
    onPlaceOrder: (String, Int, List<Pair<Medicine, Int>>) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val cartItems = CartManager.cartItems
    var selectedAddress by remember { mutableStateOf(userAddress) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showNewAddressField by remember { mutableStateOf(false) }
    var newAddressText by remember { mutableStateOf("") }

    LaunchedEffect(userAddress) {
        selectedAddress = userAddress
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        // Toolbar with systemBarsPadding to fix status bar cut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "My Cart",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Your cart is empty", color = Color.Gray, fontSize = 18.sp)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { (medicine, quantity) ->
                        CartItem(
                            medicine = medicine,
                            quantity = quantity,
                            onIncrement = { CartManager.incrementQuantity(medicine.id) },
                            onDecrement = { CartManager.decrementQuantity(medicine.id) }
                        )
                    }
                }

                // Bottom Section: Address and Place Order
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showAddressDialog = true }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF006766))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Delivery Address", fontSize = 12.sp, color = Color.Gray)
                                Text(selectedAddress, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val total = CartManager.getTotal()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Amount", fontSize = 14.sp, color = Color.Gray)
                                Text("₹$total", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
                            }
                            
                            Button(
                                onClick = { 
                                    onPlaceOrder(selectedAddress, total, cartItems.toList())
                                 },
                                modifier = Modifier
                                    .height(56.dp)
                                    .width(160.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                            ) {
                                Text("Place Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Select Delivery Address") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedAddresses.forEach { addr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedAddress = addr.address
                                    showAddressDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedAddress == addr.address, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(addr.address)
                        }
                    }
                    
                    if (showNewAddressField) {
                        OutlinedTextField(
                            value = newAddressText,
                            onValueChange = { newAddressText = it },
                            label = { Text("New Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (newAddressText.isNotBlank()) {
                                    onAddNewAddress(newAddressText)
                                    selectedAddress = newAddressText
                                    showNewAddressField = false
                                    newAddressText = ""
                                    showAddressDialog = false
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Save and Use")
                        }
                    } else {
                        TextButton(onClick = { showNewAddressField = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add New Address")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CartItem(
    medicine: Medicine,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color(0xFF006766).copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicine.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = medicine.price, color = Color(0xFF006766), fontWeight = FontWeight.Bold)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Gray)
                }
                Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF006766))
                }
            }
        }
    }
}
