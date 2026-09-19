package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun ManageDoctorsScreen(onBack: () -> Unit, onAddDoctorClick: () -> Unit, onEditDoctorClick: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    var searchQuery by remember { mutableStateOf("") }
    var doctors by remember { mutableStateOf(listOf<Doctor>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("doctors").addSnapshotListener { value, _ ->
            if (value != null) {
                val list = mutableListOf<Doctor>()
                for (doc in value.documents) {
                    val d = doc.toObject(Doctor::class.java)
                    if (d != null) {
                        list.add(d.copy(id = doc.id))
                    }
                }
                doctors = list
            }
            isLoading = false
        }
    }

    val filteredDoctors = doctors.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.department.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F8FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF006766)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(text = "Manage Doctors", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Search doctor or specialty...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF006766)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766), focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = onAddDoctorClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)), modifier = Modifier.height(56.dp)) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF006766)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(filteredDoctors) { doctor ->
                        DoctorAdminCard(
                            doctor = doctor,
                            onDelete = {
                                db.collection("doctors").document(doctor.id).delete().addOnSuccessListener {
                                    Toast.makeText(context.applicationContext, "Dr. ${doctor.name} Removed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEdit = { onEditDoctorClick(doctor.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorAdminCard(doctor: Doctor, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = doctor.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(70.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                Text(doctor.department, fontSize = 13.sp, color = Color(0xFF006766), fontWeight = FontWeight.Medium)
                Text(doctor.experience + " Experience", fontSize = 12.sp, color = Color.Gray)
                Text(doctor.hospitalName, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Options", tint = Color.Gray) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color.White)) {
                    DropdownMenuItem(text = { Text("Edit Doctor") }, leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF006766)) }, onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }, onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}
