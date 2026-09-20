package com.mr_raushan.corpuscare

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddClinicScreen(clinicId: String? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Private") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    
    val serviceCategories = listOf("Normal Health Check-up", "X-Ray", "Blood Donation")
    val subServicesMap = mapOf(
        "Normal Health Check-up" to listOf("General Checkup", "Heart Checkup", "Blood Test", "Lungs Checkup", "Stomach Checkup"),
        "X-Ray" to listOf("Chest X-Ray", "Bone X-Ray", "Dental X-Ray", "Abdominal X-Ray", "Joint X-Ray"),
        "Blood Donation" to listOf("Donate Blood", "Blood Group Check", "Find a Donor", "Nearest Blood Bank")
    )
    
    var selectedCategory by remember { mutableStateOf(serviceCategories[0]) }
    val selectedCheckups = remember { mutableStateListOf<String>() }

    LaunchedEffect(clinicId) {
        if (clinicId != null) {
            db.collection("clinics").document(clinicId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    name = doc.getString("name") ?: ""
                    address = doc.getString("address") ?: ""
                    state = doc.getString("state") ?: ""
                    city = doc.getString("city") ?: ""
                    pincode = doc.getString("pincode") ?: ""
                    contact = doc.getString("contact") ?: ""
                    type = doc.getString("type") ?: "Private"
                    existingImageUrl = doc.getString("imageUrl") ?: ""
                    val chks = doc.get("checkups") as? List<String>
                    chks?.let { selectedCheckups.clear(); selectedCheckups.addAll(it) }
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedImageUri = it }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF006766)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, enabled = !isSaving, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text(text = if (clinicId != null) "Edit Clinic" else "Add New Clinic", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF5F5F5)).clickable(enabled = !isSaving) { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (selectedImageUri == null && existingImageUrl.isBlank()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text("Upload Clinic Photo", fontSize = 14.sp, color = Color.Gray)
                        }
                    } else {
                        AsyncImage(model = selectedImageUri ?: existingImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }

                HospitalTextField(value = name, onValueChange = { name = it }, label = "Clinic Name", icon = Icons.Default.AddBusiness, enabled = !isSaving)
                HospitalTextField(value = address, onValueChange = { address = it }, label = "Address", icon = Icons.Default.LocationOn, enabled = !isSaving)
                HospitalTextField(value = pincode, onValueChange = { if (it.length <= 6) pincode = it }, label = "Pincode (6 digits)", icon = Icons.Default.PinDrop, enabled = !isSaving)

                val states = listOf("Bihar", "Delhi", "Maharashtra", "Karnataka", "Uttar Pradesh", "West Bengal")
                val citiesMap = mapOf("Bihar" to listOf("Siwan", "Chainpur", "Siswan", "Patna", "Gaya", "Muzaffarpur"), "Delhi" to listOf("New Delhi", "Dwarka", "Rohini"), "Maharashtra" to listOf("Mumbai", "Pune", "Nagpur"), "Karnataka" to listOf("Bangalore", "Mysore", "Hubli"), "Uttar Pradesh" to listOf("Lucknow", "Kanpur", "Varanasi"), "West Bengal" to listOf("Kolkata", "Siliguri", "Durgapur"))
                var stateExpanded by remember { mutableStateOf(false) }
                var cityExpanded by remember { mutableStateOf(false) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = stateExpanded && !isSaving, onExpandedChange = { if(!isSaving) stateExpanded = !stateExpanded }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = state, onValueChange = {}, readOnly = true, label = { Text("State", color = Color.Black) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp), textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766)))
                        ExposedDropdownMenu(expanded = stateExpanded, onDismissRequest = { stateExpanded = false }, modifier = Modifier.background(Color.White)) {
                            states.forEach { s -> DropdownMenuItem(text = { Text(s, color = Color.Black) }, onClick = { state = s; city = ""; stateExpanded = false }) }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = cityExpanded && !isSaving, onExpandedChange = { if (state.isNotBlank() && !isSaving) cityExpanded = !cityExpanded }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = city, onValueChange = {}, readOnly = true, label = { Text("City", color = Color.Black) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(12.dp), textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766)))
                        if (state.isNotBlank()) {
                            ExposedDropdownMenu(expanded = cityExpanded, onDismissRequest = { cityExpanded = false }, modifier = Modifier.background(Color.White)) {
                                citiesMap[state]?.forEach { c -> DropdownMenuItem(text = { Text(c, color = Color.Black) }, onClick = { city = c; cityExpanded = false }) }
                            }
                        }
                    }
                }
                
                HospitalTextField(value = contact, onValueChange = { contact = it }, label = "Contact", icon = Icons.Default.Phone, enabled = !isSaving)

                Column {
                    Text("Clinic Type", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "Private", onClick = { if(!isSaving) type = "Private" }, enabled = !isSaving, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF006766)))
                        Text("Private", color = Color.Black)
                        Spacer(modifier = Modifier.width(24.dp))
                        RadioButton(selected = type == "Government", onClick = { if(!isSaving) type = "Government" }, enabled = !isSaving, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF006766)))
                        Text("Government", color = Color.Black)
                    }
                }

                Column {
                    Text("Select Service Category", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = categoryExpanded && !isSaving, onExpandedChange = { if(!isSaving) categoryExpanded = !categoryExpanded }) {
                        OutlinedTextField(value = selectedCategory, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedBorderColor = Color(0xFF006766)))
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }, modifier = Modifier.background(Color.White)) {
                            serviceCategories.forEach { category -> DropdownMenuItem(text = { Text(category, color = Color.Black) }, onClick = { selectedCategory = category; categoryExpanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Choose Facilities in $selectedCategory", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subServicesMap[selectedCategory]?.forEach { service ->
                            FilterChip(selected = selectedCheckups.contains(service), onClick = { if(!isSaving) { if (selectedCheckups.contains(service)) selectedCheckups.remove(service) else selectedCheckups.add(service) } }, enabled = !isSaving, label = { Text(service) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF006766), selectedLabelColor = Color.White))
                        }
                    }
                    if (selectedCheckups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Selected Total: ${selectedCheckups.size}", fontSize = 12.sp, color = Color(0xFF006766), fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (name.isNotBlank() && state.isNotBlank() && city.isNotBlank() && pincode.length == 6) {
                            isSaving = true
                            val clinicData = Clinic(id = clinicId ?: "", name = name, address = address, state = state, city = city, pincode = pincode, contact = contact, type = type, checkups = selectedCheckups.toList(), imageUrl = selectedImageUri?.toString() ?: existingImageUrl)
                            val collection = db.collection("clinics")
                            
                            val task = if (clinicId != null) {
                                collection.document(clinicId).set(clinicData)
                            } else {
                                val newRef = collection.document()
                                collection.document(newRef.id).set(clinicData.copy(id = newRef.id))
                            }

                            task.addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context.applicationContext, "Clinic Data Published", Toast.LENGTH_SHORT).show()
                                onBack()
                            }.addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else { Toast.makeText(context, "Name, State, City and 6-digit Pincode are required", Toast.LENGTH_SHORT).show() }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) { 
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (clinicId != null) "Update Clinic" else "Add Clinic", fontSize = 16.sp, fontWeight = FontWeight.Bold) 
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            if (isSaving) {
                Box(modifier = Modifier.fillMaxSize().clickable(enabled = true, onClick = {}))
            }
        }
    }
}
