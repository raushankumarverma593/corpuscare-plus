package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ReportsListScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var reports by remember { mutableStateOf(listOf<MedicalReport>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).collection("reports").get()
                .addOnSuccessListener { snapshot ->
                    reports = snapshot.toObjects(MedicalReport::class.java).sortedByDescending { it.date }
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        } else {
            isLoading = false
        }
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
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text(
                text = "My Medical Reports",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006766))
            }
        } else if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No reports found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reports) { report ->
                    ReportCard(report)
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: MedicalReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = Color(0xFF006766))
                Spacer(modifier = Modifier.width(8.dp))
                Text(report.reportTitle, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Facility: ${report.facilityName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("Date: ${report.date}", fontSize = 13.sp, color = Color.Gray)
            
            if (report.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Doctor's Remarks:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(report.remarks, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Open URL placeholder */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
            ) {
                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Report")
            }
        }
    }
}
