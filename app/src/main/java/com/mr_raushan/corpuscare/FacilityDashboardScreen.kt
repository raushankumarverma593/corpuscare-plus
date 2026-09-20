package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun FacilityDashboardScreen(
    facilityId: String,
    facilityName: String,
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appointments", "Manage Reports")

    var appointments by remember { mutableStateOf(listOf<Appointment>()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Fetch Appointments for this facility
    LaunchedEffect(facilityId) {
        db.collection("appointments")
            .whereEqualTo("facilityId", facilityId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    appointments = snapshot.toObjects(Appointment::class.java).sortedByDescending { it.date }
                }
                isLoading = false
            }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout from the facility portal?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
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
        // Updated Header matching Admin style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = { showLogoutDialog = true }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White)
            }
            Text(
                text = "Facility Dashboard",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Sub-Header info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = facilityName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1E))
                Text(text = "ID: $facilityId", fontSize = 12.sp, color = Color.Gray)
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF006766),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF006766)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006766))
            }
        } else {
            when (selectedTab) {
                0 -> AppointmentList(appointments)
                1 -> ManageReportsSection(facilityId, facilityName)
            }
        }
    }
}

@Composable
fun AppointmentList(list: List<Appointment>) {
    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No appointments found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(list) { appt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF006766))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(appt.patientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Doctor: ${appt.doctorName}", fontSize = 14.sp)
                        Text("Date: ${appt.date} • Age: ${appt.patientAge}", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = Color(0xFFE0F2F1), shape = RoundedCornerShape(4.dp)) {
                            Text(appt.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, color = Color(0xFF006766), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageReportsSection(facilityId: String, facilityName: String) {
    var showAddDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    
    var patientName by remember { mutableStateOf("") }
    var patientPhone by remember { mutableStateOf("") } // Used to find userId
    var reportTitle by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload New Medical Report")
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text("Facility Instructions", fontWeight = FontWeight.Bold, color = Color(0xFF006766))
        Text("1. Enter patient's correct phone number to link report to their account.", fontSize = 12.sp, color = Color.Gray)
        Text("2. If phone not found, report will still be saved in global database.", fontSize = 12.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Activity", fontWeight = FontWeight.Bold, color = Color.Black)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Activity logs will appear here", color = Color.LightGray, fontSize = 14.sp)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { if(!isSaving) showAddDialog = false },
            title = { Text("Upload Medical Report", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Patient Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = patientPhone, onValueChange = { patientPhone = it }, label = { Text("Patient Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = reportTitle, onValueChange = { reportTitle = it }, label = { Text("Report Title (e.g. Blood Test)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Doctor Remarks") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (patientName.isNotBlank() && patientPhone.isNotBlank() && reportTitle.isNotBlank()) {
                            isSaving = true
                            // Find user by phone
                            db.collection("users").whereEqualTo("phone", patientPhone).get()
                                .addOnSuccessListener { snapshot ->
                                    val userId = if (!snapshot.isEmpty) snapshot.documents[0].id else "unknown"
                                    val report = MedicalReport(
                                        id = UUID.randomUUID().toString(),
                                        userId = userId,
                                        userName = patientName,
                                        facilityId = facilityId,
                                        facilityName = facilityName,
                                        date = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                                        reportTitle = reportTitle,
                                        remarks = remarks
                                    )
                                    
                                    db.collection("reports").document(report.id).set(report)
                                        .addOnSuccessListener {
                                            if (userId != "unknown") {
                                                db.collection("users").document(userId).collection("reports").document(report.id).set(report)
                                            }
                                            Toast.makeText(context, "Report Uploaded", Toast.LENGTH_SHORT).show()
                                            showAddDialog = false
                                            isSaving = false
                                            // Reset fields
                                            patientName = ""; patientPhone = ""; reportTitle = ""; remarks = ""
                                        }
                                }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text("Upload Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }, enabled = !isSaving) { Text("Cancel") }
            }
        )
    }
}
