package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    adminId: String,
    currentName: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    
    var name by remember { mutableStateOf(currentName) }
    var oldPasscode by remember { mutableStateOf("") }
    var newPasscode by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        // Simple Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Admin Profile",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Administrator Info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF006766)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = adminId,
                        onValueChange = {},
                        label = { Text("Admin ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            db.collection("admins").document(adminId).update("name", name)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                    ) {
                        Text("Update Name", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Passcode Security Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF006766))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Security Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF006766)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Change your access passcode", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
                    ) {
                        Text("Change Passcode", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Logout
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout Admin Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Update Passcode", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = oldPasscode,
                        onValueChange = { oldPasscode = it },
                        label = { Text("Enter Old Passcode") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newPasscode,
                        onValueChange = { newPasscode = it },
                        label = { Text("Enter New Passcode") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("admins").document(adminId).get().addOnSuccessListener { doc ->
                            if (doc.getString("passcode") == oldPasscode) {
                                db.collection("admins").document(adminId).update("passcode", newPasscode)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Passcode updated successfully", Toast.LENGTH_SHORT).show()
                                        showPasswordDialog = false
                                        oldPasscode = ""
                                        newPasscode = ""
                                    }
                            } else {
                                Toast.makeText(context, "Old passcode is incorrect", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
