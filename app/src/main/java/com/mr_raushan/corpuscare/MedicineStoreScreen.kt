package com.mr_raushan.corpuscare

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.InputStreamReader

data class MedicineCSV(
    val name: String,
    val type: String,
    val price: String,
    val manufacturer: String,
    val packSize: String,
    val introduction: String,
    val uses: String,
    val benefits: String,
    val howItWorks: String,
    val sideEffects: String,
    val howToUse: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineStoreScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    userAddress: String = "123, MG Road, Indore"
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val cartItems = CartManager.cartItems
    
    var medicines by remember { mutableStateOf(listOf<MedicineCSV>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMedicine by remember { mutableStateOf<MedicineCSV?>(null) }

    LaunchedEffect(Unit) {
        medicines = loadMedicinesFromCSV(context)
        isLoading = false
    }

    if (selectedMedicine != null) {
        MedicineDetailsScreen(
            medicine = selectedMedicine!!,
            onBack = { selectedMedicine = null },
            onAddToCart = {
                // Map MedicineCSV to Medicine for CartManager
                CartManager.addMedicine(Medicine(
                    id = selectedMedicine!!.name,
                    name = selectedMedicine!!.name,
                    price = selectedMedicine!!.price,
                    category = selectedMedicine!!.type
                ))
            },
            onCartClick = onCartClick
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
        ) {
            // Toolbar
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
                    text = "Medicine Store",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = { onCartClick() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge { Text(cartItems.sumOf { it.second }.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    placeholder = { Text("Search medicines...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF006766),
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(12.dp))

                // Categories (Types)
                val allTypes = remember(medicines) {
                    listOf("All") + medicines.map { it.type }.distinct().sorted()
                }
                var selectedType by remember { mutableStateOf("All") }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allTypes) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF006766),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedType == type,
                                borderColor = Color.LightGray,
                                selectedBorderColor = Color(0xFF006766)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF006766),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deliver to: $userAddress",
                        fontSize = 12.sp,
                        color = Color(0xFF006766),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medicine Grid
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF006766))
                    }
                } else {
                    val filteredMedicines = medicines.filter { 
                        (selectedType == "All" || it.type == selectedType) &&
                        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
                    }

                    if (filteredMedicines.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No medicines found", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filteredMedicines) { medicine ->
                                val isAdded = cartItems.any { it.first.id == medicine.name }
                                MedicineCardCSV(medicine, isAdded, onClick = { selectedMedicine = medicine }) {
                                    CartManager.addMedicine(Medicine(
                                        id = medicine.name,
                                        name = medicine.name,
                                        price = medicine.price,
                                        category = medicine.type
                                    ))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCardCSV(medicine: MedicineCSV, isAdded: Boolean, onClick: () -> Unit, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp) // Significantly reduced height
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Distribute space evenly without weight
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(45.dp) // Even smaller icon box
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Medication, null, tint = Color.LightGray, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = medicine.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                    modifier = Modifier.height(22.dp)
                )
                
                Text(
                    text = medicine.price.ifBlank { "Price N/A" },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006766)
                )
            }

            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAdded) Color(0xFF4CAF50) else Color(0xFF006766)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isAdded) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Added", fontSize = 8.sp)
                } else {
                    Text("Add", fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun MedicineDetailsScreen(medicine: MedicineCSV, onBack: () -> Unit, onAddToCart: () -> Unit, onCartClick: () -> Unit) {
    val cartItems = CartManager.cartItems
    val isAdded = cartItems.any { it.first.id == medicine.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text(
                text = "Medicine Details",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onCartClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                BadgedBox(
                    badge = {
                        if (cartItems.isNotEmpty()) {
                            Badge { Text(cartItems.sumOf { it.second }.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(medicine.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(medicine.type, fontSize = 16.sp, color = Color(0xFF006766), fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(medicine.price, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
            Text("MRP (Inclusive of all taxes)", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))
            
            DetailSection("Manufacturer", medicine.manufacturer)
            DetailSection("Pack Size", medicine.packSize)
            DetailSection("Uses", medicine.uses)
            DetailSection("Benefits", medicine.benefits)
            DetailSection("How it Works", medicine.howItWorks)
            DetailSection("Side Effects", medicine.sideEffects)
            DetailSection("How to Use", medicine.howToUse)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Introduction", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text(medicine.introduction, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Bottom Bar with Add to Cart
        Surface(
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Price", fontSize = 12.sp, color = Color.Gray)
                    Text(medicine.price, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006766))
                }
                Button(
                    onClick = { if (!isAdded) onAddToCart() },
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdded) Color(0xFF4CAF50) else Color(0xFF006766)
                    )
                ) {
                    Icon(if (isAdded) Icons.Default.Check else Icons.Default.AddShoppingCart, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAdded) "Added to Cart" else "Add to Cart")
                }
            }
        }
    }
}

@Composable
fun DetailSection(label: String, value: String) {
    if (value.isNotBlank()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Text(value, fontSize = 14.sp, color = Color.Gray)
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFF0F0F0))
        }
    }
}

fun splitCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val char = line[i]
        if (char == '\"') {
            inQuotes = !inQuotes
        } else if (char == ',' && !inQuotes) {
            result.add(current.toString().trim())
            current.setLength(0)
        } else {
            current.append(char)
        }
        i++
    }
    result.add(current.toString().trim())
    return result
}

fun loadMedicinesFromCSV(context: Context): List<MedicineCSV> {
    val medicines = mutableListOf<MedicineCSV>()
    try {
        val inputStream = context.assets.open("medicine_list_clean.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        // Skip header
        reader.readLine()
        
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val tokens = splitCsvLine(line ?: "")
            if (tokens.size >= 11) {
                medicines.add(
                    MedicineCSV(
                        name = tokens[0],
                        type = tokens[1],
                        price = tokens[2],
                        manufacturer = tokens[3],
                        packSize = tokens[4],
                        introduction = tokens[5],
                        uses = tokens[6],
                        benefits = tokens[7],
                        howItWorks = tokens[8],
                        sideEffects = tokens[9],
                        howToUse = tokens[10]
                    )
                )
            }
        }
        reader.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return medicines
}
