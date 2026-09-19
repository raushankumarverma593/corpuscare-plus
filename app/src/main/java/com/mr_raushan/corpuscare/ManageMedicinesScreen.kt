package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageMedicinesScreen(onBack: () -> Unit, onAddMedicineClick: () -> Unit, onEditMedicineClick: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    var searchQuery by remember { mutableStateOf("") }
    var medicines by remember { mutableStateOf(listOf<Medicine>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("medicines_store").addSnapshotListener { value, _ ->
            if (value != null) {
                val list = mutableListOf<Medicine>()
                for (doc in value.documents) {
                    val m = doc.toObject(Medicine::class.java)
                    if (m != null) {
                        list.add(m.copy(id = doc.id))
                    }
                }
                medicines = list
            }
            isLoading = false
        }
    }

    val filteredMedicines = medicines.filter { 
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F8FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF006766)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(text = "Manage Medicines", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Search medicine...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF006766)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766), focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = onAddMedicineClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)), modifier = Modifier.height(56.dp)) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF006766)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(filteredMedicines) { medicine ->
                        MedicineAdminCard(
                            medicine = medicine,
                            onDelete = {
                                db.collection("medicines_store").document(medicine.id).delete().addOnSuccessListener {
                                    Toast.makeText(context.applicationContext, "${medicine.name} Removed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEdit = { onEditMedicineClick(medicine.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineAdminCard(medicine: Medicine, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = medicine.imageUrl, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(medicine.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                Text(medicine.price, fontSize = 15.sp, color = Color(0xFF006766), fontWeight = FontWeight.Bold)
                Text(medicine.shopName, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(medicine.shopAddress, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Options", tint = Color.Gray) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color.White)) {
                    DropdownMenuItem(text = { Text("Edit Medicine") }, leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF006766)) }, onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }, onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}
