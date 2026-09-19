package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

data class Facility(
    val id: String,
    val name: String,
    val address: String,
    val rating: String,
    val distance: String,
    val imageUrl: String = "",
    val imageRes: Int? = null
)

@Composable
fun FacilityListScreen(
    serviceName: String,
    userPincode: String,
    onBack: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    var facilities by remember { mutableStateOf(listOf<Facility>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(serviceName, userPincode) {
        val results = mutableListOf<Facility>()
        val userPin = userPincode.trim()

        // 1. Hardcoded Local Storage Data for Testing
        val localHospitals = listOf(
            Hospital(
                id = "local_hosp_1",
                name = "City Care Life Hospital",
                address = "Near Main Market, Sector 4",
                pincode = "801506",
                contact = "9876543210",
                type = "Private",
                checkups = listOf("General Checkup", "Heart Checkup", "Blood Test", "Lungs Checkup", "Stomach Checkup", "Chest X-Ray", "Bone X-Ray", "Donate Blood")
            ),
            Hospital(
                id = "local_hosp_2",
                name = "Sanjeevani Health Clinic & Hospital",
                address = "Station Road, Opposite Axis Bank",
                pincode = "841203",
                contact = "8888877777",
                type = "Government",
                checkups = listOf("General Checkup", "Blood Test", "Chest X-Ray", "Donate Blood", "Blood Group Check")
            ),
            Hospital(
                id = "local_hosp_3",
                name = "Apex Medical Centre",
                address = "Bypass Road, Near Hanuman Temple",
                pincode = "841203",
                contact = "7777766666",
                type = "Private",
                checkups = listOf("General Checkup", "Heart Checkup", "Bone X-Ray", "Dental X-Ray", "Find a Donor")
            )
        )

        val localClinics = listOf(
            Clinic(
                id = "local_clinic_1",
                name = "Raushan Dental & Diagnostic Clinic",
                address = "123, VIP Colony Main Road",
                pincode = "841203",
                contact = "9999988888",
                type = "Private",
                checkups = listOf("General Checkup", "Dental X-Ray", "Blood Test")
            ),
            Clinic(
                id = "local_clinic_2",
                name = "Durga Medical & X-Ray Lab",
                address = "Palika Bazar, Ground Floor",
                pincode = "841203",
                contact = "6666655555",
                type = "Private",
                checkups = listOf("Chest X-Ray", "Bone X-Ray", "Abdominal X-Ray", "General Checkup")
            )
        )

        // Process Local Hospitals
        localHospitals.forEach { h ->
            val hospPin = h.pincode.trim()
            val matchesPin = userPin.isNotBlank() && userPin == hospPin
            val normalizedQuery = serviceName.trim().lowercase().replace(" ", "").replace("-", "")
            val hasService = h.checkups.any { 
                val normalizedService = it.trim().lowercase().replace(" ", "").replace("-", "")
                normalizedService.contains(normalizedQuery) || normalizedQuery.contains(normalizedService)
            }
            if (matchesPin && hasService) {
                results.add(Facility(
                    id = h.id,
                    name = h.name,
                    address = h.address,
                    rating = h.contact,
                    distance = h.type,
                    imageUrl = h.imageUrl
                ))
            }
        }

        // Process Local Clinics
        localClinics.forEach { c ->
            val clinicPin = c.pincode.trim()
            val matchesPin = userPin.isNotBlank() && userPin == clinicPin
            val normalizedQuery = serviceName.trim().lowercase().replace(" ", "").replace("-", "")
            val hasService = c.checkups.any { 
                val normalizedService = it.trim().lowercase().replace(" ", "").replace("-", "")
                normalizedService.contains(normalizedQuery) || normalizedQuery.contains(normalizedService)
            }
            if (matchesPin && hasService) {
                results.add(Facility(
                    id = c.id,
                    name = c.name,
                    address = c.address,
                    rating = c.contact,
                    distance = c.type,
                    imageUrl = c.imageUrl
                ))
            }
        }

        // 2. Fetch from Firebase Firestore as fallback / combined data
        db.collection("hospitals").get().addOnSuccessListener { hDocs ->
            hDocs.documents.forEach { doc ->
                val h = doc.toObject(Hospital::class.java)
                if (h != null && results.none { it.id == doc.id }) {
                    val hospPin = h.pincode.trim()
                    val matchesPin = userPin.isNotBlank() && userPin == hospPin
                    val normalizedQuery = serviceName.trim().lowercase().replace(" ", "").replace("-", "")
                    val hasService = h.checkups.any { 
                        val normalizedService = it.trim().lowercase().replace(" ", "").replace("-", "")
                        normalizedService.contains(normalizedQuery) || normalizedQuery.contains(normalizedService)
                    }
                    
                    if (matchesPin && hasService) {
                        results.add(Facility(
                            id = doc.id,
                            name = h.name,
                            address = h.address,
                            rating = h.contact,
                            distance = h.type,
                            imageUrl = h.imageUrl
                        ))
                    }
                }
            }
            
            db.collection("clinics").get().addOnSuccessListener { cDocs ->
                cDocs.documents.forEach { doc ->
                    val c = doc.toObject(Clinic::class.java)
                    if (c != null && results.none { it.id == doc.id }) {
                        val clinicPin = c.pincode.trim()
                        val matchesPin = userPin.isNotBlank() && userPin == clinicPin
                        val normalizedQuery = serviceName.trim().lowercase().replace(" ", "").replace("-", "")
                        val hasService = c.checkups.any { 
                            val normalizedService = it.trim().lowercase().replace(" ", "").replace("-", "")
                            normalizedService.contains(normalizedQuery) || normalizedQuery.contains(normalizedService)
                        }

                        if (matchesPin && hasService) {
                            results.add(Facility(
                                id = doc.id,
                                name = c.name,
                                address = c.address,
                                rating = c.contact,
                                distance = c.type,
                                imageUrl = c.imageUrl
                            ))
                        }
                    }
                }
                facilities = results.toList()
                isLoading = false
            }.addOnFailureListener {
                facilities = results.toList()
                isLoading = false
            }
        }.addOnFailureListener {
            facilities = results.toList()
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F8FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF006766)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(text = serviceName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF006766))
            }
        } else if (facilities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.LocationOff, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No facilities found in your area (Pincode: ${userPincode.ifBlank { "Not Set" }})", 
                        color = Color.Gray, 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Service: $serviceName",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp), 
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(facilities) { facility ->
                    FacilityItem(facility)
                }
            }
        }
    }
}

@Composable
fun FacilityItem(facility: Facility) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = facility.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F2F5)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = facility.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = facility.address, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Surface(color = if (facility.distance == "Government") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(6.dp)) {
                            Text(text = facility.distance, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = if (facility.distance == "Government") Color(0xFF4CAF50) else Color(0xFFE57373), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${facility.name} ${facility.address}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006766)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF006766))
                ) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Locate", fontSize = 14.sp)
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:${facility.rating}")
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call", fontSize = 14.sp)
                }
            }
        }
    }
}
