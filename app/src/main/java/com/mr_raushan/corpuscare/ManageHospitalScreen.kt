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
fun ManageHospitalScreen(onBack: () -> Unit, onAddHospitalClick: () -> Unit, onEditHospitalClick: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    var searchQuery by remember { mutableStateOf("") }
    var hospitals by remember { mutableStateOf(listOf<Hospital>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Hospitals
    LaunchedEffect(Unit) {
        db.collection("hospitals").addSnapshotListener { value, error ->
            if (value != null) {
                val list = mutableListOf<Hospital>()
                for (doc in value.documents) {
                    val h = doc.toObject(Hospital::class.java)
                    if (h != null) {
                        list.add(h.copy(id = doc.id))
                    }
                }
                hospitals = list
            }
            isLoading = false
        }
    }

    val filteredHospitals = hospitals.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.city.contains(searchQuery, ignoreCase = true)
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
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                text = "Manage Hospitals",
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
                    placeholder = { Text("Search hospital...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF006766)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF006766),
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onAddHospitalClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF006766))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredHospitals) { hospital ->
                        HospitalAdminCard(
                            hospital = hospital,
                            onDelete = {
                                if (hospital.id.isNotBlank()) {
                                    val hid = hospital.id
                                    val hname = hospital.name
                                    db.collection("hospitals").document(hid).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context.applicationContext, "$hname Removed", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context.applicationContext, "Delete failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Cannot delete: Hospital ID missing", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEdit = {
                                onEditHospitalClick(hospital.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalAdminCard(hospital: Hospital, onDelete: () -> Unit, onEdit: () -> Unit) {
    val borderColor = if (hospital.type == "Government") Color(0xFF4CAF50) else Color(0xFFE57373)
    val bgColor = if (hospital.type == "Government") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = hospital.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("ID: ${hospital.id}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(hospital.city + ", " + hospital.state, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        hospital.type, 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                        text = { Text("Edit Hospital") },
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
