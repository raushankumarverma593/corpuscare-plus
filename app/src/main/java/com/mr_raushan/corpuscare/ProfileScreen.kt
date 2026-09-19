package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.location.Geocoder
import java.util.Locale

data class UserAddress(
    val id: String,
    val address: String,
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    currentName: String,
    currentPhone: String,
    currentAge: String,
    currentBloodGroup: String,
    currentEmail: String,
    addresses: List<UserAddress>,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onAddAddress: (String) -> Unit,
    onEditAddress: (String, String) -> Unit,
    onSetDefaultAddress: (String) -> Unit,
    onDeleteAddress: (String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentName) }
    var age by remember { mutableStateOf(currentAge) }
    var bloodGroup by remember { mutableStateOf(currentBloodGroup) }
    var email by remember { mutableStateOf(currentEmail) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var addressToEdit by remember { mutableStateOf<UserAddress?>(null) }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Not Selected")

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
                text = "My Profile",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF006766))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = currentPhone, 
                            onValueChange = {}, 
                            label = { Text("Phone Number") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = bloodGroupExpanded,
                                onExpandedChange = { bloodGroupExpanded = !bloodGroupExpanded },
                                modifier = Modifier.weight(1.5f)
                            ) {
                                OutlinedTextField(
                                    value = bloodGroup.ifBlank { "Not Selected" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Blood Group") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = bloodGroupExpanded,
                                    onDismissRequest = { bloodGroupExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    bloodGroups.forEach { bg ->
                                        DropdownMenuItem(
                                            text = { Text(bg, color = Color.Black) },
                                            onClick = {
                                                bloodGroup = if (bg == "Not Selected") "" else bg
                                                bloodGroupExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { 
                                onUpdateProfile(name, age, bloodGroup, email)
                                Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                        ) {
                            Text("Update Profile")
                        }
                    }
                }
            }

            // Addresses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Addresses", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { showAddressDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Address", tint = Color(0xFF006766))
                    }
                }
            }

            if (addresses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocationOff, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Not saved address", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(addresses) { addr ->
                    AddressItem(
                        address = addr,
                        onSetDefault = { onSetDefaultAddress(addr.id) },
                        onEdit = { addressToEdit = addr },
                        onDelete = { onDeleteAddress(addr.id) }
                    )
                }
            }

            // Logout Button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddressDialog || addressToEdit != null) {
        var addressLine1 by remember { mutableStateOf(addressToEdit?.address?.split(",")?.getOrNull(0)?.trim() ?: "") }
        var landmark by remember { mutableStateOf(addressToEdit?.address?.split(",")?.getOrNull(1)?.trim() ?: "") }
        var pincode by remember { mutableStateOf(addressToEdit?.address?.split("-")?.lastOrNull()?.trim() ?: "") }
        var city by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }

        // Attempt to parse city/state if editing
        LaunchedEffect(addressToEdit) {
            addressToEdit?.let { addr ->
                val parts = addr.address.split(",")
                if (parts.size >= 3) {
                    val cityPart = parts[2].trim()
                    if (cityPart.contains("-")) {
                        city = cityPart.split("-")[0].trim()
                    } else {
                        city = cityPart
                    }
                }
                val statePart = parts.getOrNull(3)?.trim()
                if (statePart != null) {
                    if (statePart.contains("-")) {
                        state = statePart.split("-")[0].trim()
                    } else {
                        state = statePart
                    }
                }
            }
        }

        // Mock pincode data
        val pincodeMap = mapOf(
            "452001" to ("Indore" to "Madhya Pradesh"),
            "110001" to ("New Delhi" to "Delhi"),
            "400001" to ("Mumbai" to "Maharashtra"),
            "560001" to ("Bangalore" to "Karnataka"),
            "800001" to ("Patna" to "Bihar"),
            "700001" to ("Kolkata" to "West Bengal"),
            "600001" to ("Chennai" to "Tamil Nadu")
        )

        // Search City/State by Pincode Logic
        LaunchedEffect(pincode) {
            if (pincode.length == 6) {
                // First check local mock map
                val localData = pincodeMap[pincode]
                if (localData != null) {
                    city = localData.first
                    state = localData.second
                } else {
                    // Try Geocoder as fallback for any 6-digit pincode
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocationName(pincode, 1)
                        if (!addresses.isNullOrEmpty()) {
                            city = addresses[0].locality ?: addresses[0].subAdminArea ?: ""
                            state = addresses[0].adminArea ?: ""
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { 
                showAddressDialog = false 
                addressToEdit = null
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { 
                Text(
                    if (addressToEdit != null) "Edit Address" else "Add New Address", 
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006766)
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Use Current Location Button
                    Button(
                        onClick = {
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                            try {
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { location ->
                                        if (location != null) {
                                            val geocoder = Geocoder(context, Locale.getDefault())
                                            try {
                                                val foundAddresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                                if (!foundAddresses.isNullOrEmpty()) {
                                                    val addr = foundAddresses[0]
                                                    pincode = addr.postalCode ?: ""
                                                    city = addr.locality ?: addr.subAdminArea ?: ""
                                                    state = addr.adminArea ?: ""
                                                    addressLine1 = addr.getAddressLine(0).split(",")[0]
                                                    landmark = addr.subLocality ?: ""
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                            } catch (e: SecurityException) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1), contentColor = Color(0xFF006766))
                    ) {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-detect Location", fontWeight = FontWeight.Bold)
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)

                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { if (it.length <= 6) pincode = it },
                        label = { Text("Pincode") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF006766),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false, // Truly block interaction
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false, // Truly block interaction
                            label = { Text("State") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                    }

                    OutlinedTextField(
                        value = addressLine1,
                        onValueChange = { addressLine1 = it },
                        label = { Text("Address Line 1 (Street/Flat/House No)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
                    )

                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addressLine1.isNotBlank() && pincode.isNotBlank() && city.isNotBlank()) {
                            val fullAddress = "$addressLine1, $landmark, $city, $state - $pincode".replace(", ,", ",")
                            if (addressToEdit != null) {
                                onEditAddress(addressToEdit!!.id, fullAddress)
                                addressToEdit = null
                            } else {
                                onAddAddress(fullAddress)
                                showAddressDialog = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) {
                    Text(if (addressToEdit != null) "Update Address" else "Save Address", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddressDialog = false 
                        addressToEdit = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AddressItem(address: UserAddress, onSetDefault: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (address.isDefault) Color(0xFFE0F2F1) else Color.White
        ),
        border = if (address.isDefault) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006766)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (address.isDefault) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (address.isDefault) Color(0xFF006766) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (address.isDefault) {
                    Text("Default Address", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006766))
                }
                Text(text = address.address, fontSize = 14.sp, color = if (address.isDefault) Color.Black else Color.DarkGray)
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (!address.isDefault) {
                        DropdownMenuItem(
                            text = { Text("Set as Default") },
                            leadingIcon = { Icon(Icons.Default.Check, null) },
                            onClick = {
                                onSetDefault()
                                showMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
