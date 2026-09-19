package com.mr_raushan.corpuscare

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Doctor data class is now centrally managed in ManageDoctorsScreen.kt

@Composable
fun DoctorListScreen(
    departmentName: String,
    userPincode: String,
    onBack: () -> Unit,
    onDoctorClick: (Doctor) -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    var doctors by remember { mutableStateOf(listOf<Doctor>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(departmentName, userPincode) {
        isLoading = true // Start loading immediately
        val list = mutableListOf<Doctor>()
        val userPin = userPincode.trim()

        // Process Local Doctors from BookingManager
        BookingManager.localDoctors.forEach { d ->
            val matchesDept = d.department.equals(departmentName, ignoreCase = true)
            val matchesPin = userPin.isNotBlank() && d.pincode.trim() == userPin
            if (matchesDept && matchesPin) {
                list.add(d)
            }
        }

        // Fetch from Firebase Firestore as fallback
        db.collection("doctors").get().addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEach { doc ->
                val d = doc.toObject(Doctor::class.java)
                if (d != null && list.none { it.id == doc.id || it.name == d.name }) {
                    val matchesDept = d.department.equals(departmentName, ignoreCase = true)
                    val matchesPin = userPin.isNotBlank() && d.pincode.trim() == userPin
                    if (matchesDept && matchesPin) {
                        list.add(d.copy(id = doc.id))
                    }
                }
            }
            doctors = list.toList()
            isLoading = false
        }.addOnFailureListener {
            doctors = list.toList()
            isLoading = false
        }
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
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = departmentName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sorting & Filter Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${doctors.size} Doctors Available",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Row(
                modifier = Modifier
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort By", fontSize = 12.sp, color = Color.Gray)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
            }
        }

        // Doctor List or Loading/Empty State
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006766))
            }
        } else if (doctors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No doctors found in your area ($userPincode)",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(doctors) { doctor ->
                    DoctorCard(doctor = doctor) { onDoctorClick(doctor) }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Doctor Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "${doctor.department} • ${doctor.experience}",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Business, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = doctor.hospitalName, fontSize = 12.sp, color = Color.Gray)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFF006766), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = doctor.rating, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006766))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = doctor.city, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}
