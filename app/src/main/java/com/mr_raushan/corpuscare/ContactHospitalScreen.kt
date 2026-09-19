package com.mr_raushan.corpuscare

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HospitalContact(
    val name: String,
    val address: String,
    val phone: String,
    val city: String,
    val state: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactHospitalScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    val states = listOf("Bihar", "Delhi", "Maharashtra", "Karnataka", "Uttar Pradesh", "West Bengal")
    val citiesMap = mapOf(
        "Bihar" to listOf("Siwan", "Chainpur", "Siswan", "Patna", "Gaya", "Muzaffarpur"),
        "Delhi" to listOf("New Delhi", "Dwarka", "Rohini"),
        "Maharashtra" to listOf("Mumbai", "Pune", "Nagpur"),
        "Karnataka" to listOf("Bangalore", "Mysore", "Hubli"),
        "Uttar Pradesh" to listOf("Lucknow", "Kanpur", "Varanasi"),
        "West Bengal" to listOf("Kolkata", "Siliguri", "Durgapur")
    )

    val allHospitals = emptyList<HospitalContact>() // Cleared dummy hospitals

    var selectedState by remember { mutableStateOf("Bihar") }
    var selectedCity by remember { mutableStateOf("Patna") }
    var stateExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    val filteredHospitals = allHospitals.filter { 
        it.state == selectedState && it.city == selectedCity 
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
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Contact Hospital",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Filter by Location", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    // State Selector
                    ExposedDropdownMenuBox(
                        expanded = stateExpanded,
                        onExpandedChange = { stateExpanded = !stateExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Color(0xFF006766),
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF006766),
                                unfocusedLabelColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            states.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state, color = Color.Black) },
                                    onClick = {
                                        selectedState = state
                                        selectedCity = citiesMap[state]?.firstOrNull() ?: ""
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // City Selector
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = !cityExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("City") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Color(0xFF006766),
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF006766),
                                unfocusedLabelColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            citiesMap[selectedState]?.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city, color = Color.Black) },
                                    onClick = {
                                        selectedCity = city
                                        cityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hospital List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredHospitals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hospitals found for this location", color = Color.Gray)
                    }
                }
            }
            
            items(filteredHospitals) { hospital ->
                HospitalContactCard(hospital) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${hospital.phone}")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun HospitalContactCard(hospital: HospitalContact, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hospital Image Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF006766),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hospital.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hospital.address,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onCallClick,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Now", fontSize = 14.sp)
                }
            }
        }
    }
}
