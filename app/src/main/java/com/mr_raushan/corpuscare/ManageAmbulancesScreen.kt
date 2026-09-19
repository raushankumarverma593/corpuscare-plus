package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageAmbulancesScreen(onBack: () -> Unit, onAddAmbulanceClick: () -> Unit, onEditAmbulanceClick: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    var searchQuery by remember { mutableStateOf("") }
    var ambulances by remember { mutableStateOf(listOf<Ambulance>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Ambulances
    LaunchedEffect(Unit) {
        db.collection("ambulances").addSnapshotListener { value, error ->
            if (value != null) {
                val list = mutableListOf<Ambulance>()
                for (doc in value.documents) {
                    val a = doc.toObject(Ambulance::class.java)
                    if (a != null) {
                        list.add(a.copy(id = doc.id))
                    }
                }
                ambulances = list
            }
            isLoading = false
        }
    }

    val filteredAmbulances = ambulances.filter { 
        it.driverName.contains(searchQuery, ignoreCase = true) || 
        it.vehicleNumber.contains(searchQuery, ignoreCase = true) || 
        it.pincode.contains(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        // Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF1A1C1E))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                text = "Manage Ambulances",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Search and Add Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ambulance...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF1A1C1E)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1C1E),
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onAddAmbulanceClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1A1C1E))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAmbulances) { ambulance ->
                        AmbulanceAdminCard(
                            ambulance = ambulance,
                            onDelete = {
                                if (ambulance.id.isNotBlank()) {
                                    db.collection("ambulances").document(ambulance.id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context.applicationContext, "Ambulance Removed", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            onEdit = {
                                onEditAmbulanceClick(ambulance.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbulanceAdminCard(ambulance: Ambulance, onDelete: () -> Unit, onEdit: () -> Unit) {
    val statusColor = if (ambulance.status == "Available") Color(0xFF4CAF50) else Color(0xFFFF9800)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalShipping, null, tint = Color(0xFF006766), modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ambulance.driverName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(ambulance.vehicleNumber, fontSize = 14.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(ambulance.pincode, fontSize = 13.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            ambulance.type, 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            ambulance.status, 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Options", tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Ambulance") },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF006766)) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
