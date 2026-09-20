package com.mr_raushan.corpuscare

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDoctorScreen(doctorId: String? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var selectedFacilityId by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }

    var hospitalList by remember { mutableStateOf(listOf<Hospital>()) }
    var clinicList by remember { mutableStateOf(listOf<Clinic>()) }

    val deptOptions = listOf("Cardiology", "Orthopedics", "ENT", "General Medicine", "Dermatology", "Neurology", "Psychiatry", "Pediatrics")

    LaunchedEffect(Unit) {
        db.collection("hospitals").get().addOnSuccessListener { snapshot ->
            hospitalList = snapshot.toObjects(Hospital::class.java)
        }
        db.collection("clinics").get().addOnSuccessListener { snapshot ->
            clinicList = snapshot.toObjects(Clinic::class.java)
        }
    }

    LaunchedEffect(doctorId) {
        if (doctorId != null) {
            db.collection("doctors").document(doctorId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    name = doc.getString("name") ?: ""
                    hospitalName = doc.getString("hospitalName") ?: ""
                    selectedFacilityId = doc.getString("facilityId") ?: ""
                    experience = doc.getString("experience") ?: ""
                    department = doc.getString("department") ?: ""
                    state = doc.getString("state") ?: ""
                    city = doc.getString("city") ?: ""
                    pincode = doc.getString("pincode") ?: ""
                    existingImageUrl = doc.getString("imageUrl") ?: ""
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedImageUri = it }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF006766)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, enabled = !isSaving, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text(text = if (doctorId != null) "Edit Doctor Info" else "Add New Doctor", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Circle Image Picker for Doctor
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFF5F5F5)).clickable(enabled = !isSaving) { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri == null && existingImageUrl.isBlank()) {
                            Icon(Icons.Default.PersonAdd, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        } else {
                            AsyncImage(model = selectedImageUri ?: existingImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                }

                HospitalTextField(value = name, onValueChange = { name = it }, label = "Doctor Full Name", icon = Icons.Default.Person, enabled = !isSaving)
                
                // Hospital/Clinic Selection
                var facilityExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = facilityExpanded && !isSaving, onExpandedChange = { if(!isSaving) facilityExpanded = !facilityExpanded }) {
                    OutlinedTextField(
                        value = hospitalName, onValueChange = {}, readOnly = true, label = { Text("Select Hospital/Clinic", color = Color.Black) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facilityExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766))
                    )
                    ExposedDropdownMenu(expanded = facilityExpanded, onDismissRequest = { facilityExpanded = false }, modifier = Modifier.background(Color.White)) {
                        Text("Hospitals", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        hospitalList.forEach { h ->
                            DropdownMenuItem(text = { Text(h.name, color = Color.Black) }, onClick = { 
                                hospitalName = h.name
                                selectedFacilityId = h.id
                                pincode = h.pincode
                                city = h.city
                                state = h.state
                                facilityExpanded = false 
                            })
                        }
                        HorizontalDivider()
                        Text("Clinics", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                        clinicList.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name, color = Color.Black) }, onClick = { 
                                hospitalName = c.name
                                selectedFacilityId = c.id
                                pincode = c.pincode
                                city = c.city
                                state = c.state
                                facilityExpanded = false 
                            })
                        }
                    }
                }
                
                HospitalTextField(
                    value = pincode, 
                    onValueChange = { if (it.length <= 6) pincode = it }, 
                    label = "Pincode (6 digits)", 
                    icon = Icons.Default.PinDrop,
                    enabled = !isSaving
                )

                HospitalTextField(value = experience, onValueChange = { experience = it }, label = "Experience (e.g. 10 Yrs)", icon = Icons.Default.History, enabled = !isSaving)

                // Specialty / Department Dropdown
                var deptExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = deptExpanded && !isSaving, onExpandedChange = { if(!isSaving) deptExpanded = !deptExpanded }) {
                    OutlinedTextField(
                        value = department, onValueChange = {}, readOnly = true, label = { Text("Specialty Department", color = Color.Black) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766))
                    )
                    ExposedDropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }, modifier = Modifier.background(Color.White)) {
                        deptOptions.forEach { d -> DropdownMenuItem(text = { Text(d, color = Color.Black) }, onClick = { department = d; deptExpanded = false }) }
                    }
                }

                // Location (State/City)
                val states = listOf("Bihar", "Delhi", "Maharashtra", "Karnataka", "Uttar Pradesh", "West Bengal")
                val citiesMap = mapOf("Bihar" to listOf("Siwan", "Chainpur", "Siswan", "Patna", "Gaya", "Muzaffarpur"), "Delhi" to listOf("New Delhi", "Dwarka", "Rohini"), "Maharashtra" to listOf("Mumbai", "Pune", "Nagpur"), "Karnataka" to listOf("Bangalore", "Mysore", "Hubli"), "Uttar Pradesh" to listOf("Lucknow", "Kanpur", "Varanasi"), "West Bengal" to listOf("Kolkata", "Siliguri", "Durgapur"))
                var stateExpanded by remember { mutableStateOf(false) }
                var cityExpanded by remember { mutableStateOf(false) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = stateExpanded && !isSaving, onExpandedChange = { if(!isSaving) stateExpanded = !stateExpanded }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = state, onValueChange = {}, readOnly = true, label = { Text("State", color = Color.Black) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp), textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766)))
                        ExposedDropdownMenu(expanded = stateExpanded, onDismissRequest = { stateExpanded = false }, modifier = Modifier.background(Color.White)) {
                            states.forEach { s -> DropdownMenuItem(text = { Text(s, color = Color.Black) }, onClick = { state = s; city = ""; stateExpanded = false }) }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = cityExpanded && !isSaving, onExpandedChange = { if (state.isNotBlank() && !isSaving) cityExpanded = !cityExpanded }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = city, onValueChange = {}, readOnly = true, label = { Text("City", color = Color.Black) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp), textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766)))
                        if (state.isNotBlank()) {
                            ExposedDropdownMenu(expanded = cityExpanded, onDismissRequest = { cityExpanded = false }, modifier = Modifier.background(Color.White)) {
                                citiesMap[state]?.forEach { c -> DropdownMenuItem(text = { Text(c, color = Color.Black) }, onClick = { city = c; cityExpanded = false }) }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (name.isNotBlank() && department.isNotBlank() && selectedFacilityId.isNotBlank()) {
                            isSaving = true
                            val doctorData = Doctor(
                                id = doctorId ?: "",
                                name = name,
                                hospitalName = hospitalName,
                                facilityId = selectedFacilityId,
                                experience = experience,
                                department = department,
                                city = city,
                                state = state,
                                pincode = pincode,
                                imageUrl = selectedImageUri?.toString() ?: existingImageUrl
                            )
                            val collection = db.collection("doctors")
                            
                            val task = if (doctorId != null) {
                                collection.document(doctorId).set(doctorData)
                            } else {
                                val newRef = collection.document()
                                collection.document(newRef.id).set(doctorData.copy(id = newRef.id))
                            }

                            task.addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context.applicationContext, "Doctor Saved Successfully", Toast.LENGTH_SHORT).show()
                                onBack()
                            }.addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else { Toast.makeText(context, "Name, Specialty, and Facility are required", Toast.LENGTH_SHORT).show() }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) { 
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Save Doctor Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold) 
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            if (isSaving) {
                Box(modifier = Modifier.fillMaxSize().clickable(enabled = true, onClick = {}))
            }
        }
    }
}
