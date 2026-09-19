package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAmbulanceScreen(ambulanceId: String? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var isSaving by remember { mutableStateOf(false) }

    var driverName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Normal") }
    var status by remember { mutableStateOf("Available") }

    val typeOptions = listOf("Normal", "Oxygen", "Ventilator")
    val statusOptions = listOf("Available", "Busy")

    LaunchedEffect(ambulanceId) {
        if (ambulanceId != null) {
            db.collection("ambulances").document(ambulanceId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    driverName = doc.getString("driverName") ?: ""
                    vehicleNumber = doc.getString("vehicleNumber") ?: ""
                    contact = doc.getString("contact") ?: ""
                    pincode = doc.getString("pincode") ?: ""
                    type = doc.getString("type") ?: "Normal"
                    status = doc.getString("status") ?: "Available"
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF1A1C1E)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, enabled = !isSaving, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(text = if (ambulanceId != null) "Edit Ambulance" else "Add New Ambulance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                
                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving,
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1C1E),
                        focusedLabelColor = Color(0xFF1A1C1E)
                    )
                )

                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Vehicle Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving,
                    leadingIcon = { Icon(Icons.Default.LocalShipping, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1C1E),
                        focusedLabelColor = Color(0xFF1A1C1E)
                    )
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving,
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1C1E),
                        focusedLabelColor = Color(0xFF1A1C1E)
                    )
                )

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { if (it.length <= 6) pincode = it },
                    label = { Text("Service Pincode") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving,
                    leadingIcon = { Icon(Icons.Default.PinDrop, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A1C1E),
                        focusedLabelColor = Color(0xFF1A1C1E)
                    )
                )

                Column {
                    Text("Ambulance Type", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        typeOptions.forEach { option ->
                            FilterChip(
                                selected = type == option,
                                onClick = { type = option },
                                label = { Text(option) },
                                enabled = !isSaving,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF006766), selectedLabelColor = Color.White)
                            )
                        }
                    }
                }

                Column {
                    Text("Availability Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        statusOptions.forEach { option ->
                            FilterChip(
                                selected = status == option,
                                onClick = { status = option },
                                label = { Text(option) },
                                enabled = !isSaving,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if (option == "Available") Color(0xFF4CAF50) else Color(0xFFFF9800), selectedLabelColor = Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (driverName.isNotBlank() && vehicleNumber.isNotBlank() && contact.isNotBlank() && pincode.length == 6) {
                            isSaving = true
                            val ambulanceData = Ambulance(
                                id = ambulanceId ?: "",
                                driverName = driverName,
                                vehicleNumber = vehicleNumber,
                                contact = contact,
                                pincode = pincode,
                                type = type,
                                status = status
                            )
                            
                            val docRef = if (ambulanceId != null) {
                                db.collection("ambulances").document(ambulanceId)
                            } else {
                                db.collection("ambulances").document()
                            }
                            
                            val finalData = if (ambulanceId == null) ambulanceData.copy(id = docRef.id) else ambulanceData
                            
                            docRef.set(finalData).addOnCompleteListener { task ->
                                isSaving = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context.applicationContext, "Ambulance Data Saved", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "Failed to save: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (ambulanceId != null) "Update Ambulance" else "Register Ambulance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (isSaving) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)).clickable(enabled = false) {})
            }
        }
    }
}
