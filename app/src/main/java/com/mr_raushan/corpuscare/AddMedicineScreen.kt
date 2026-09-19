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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(medicineId: String? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(medicineId) {
        if (medicineId != null) {
            db.collection("medicines_store").document(medicineId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    name = doc.getString("name") ?: ""
                    shopName = doc.getString("shopName") ?: ""
                    shopAddress = doc.getString("shopAddress") ?: ""
                    price = doc.getString("price") ?: ""
                    existingImageUrl = doc.getString("imageUrl") ?: ""
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedImageUri = it }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().background(color = Color(0xFF006766)).statusBarsPadding().height(70.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack, enabled = !isSaving, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text(text = if (medicineId != null) "Edit Medicine" else "Add New Medicine", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF5F5F5)).clickable(enabled = !isSaving) { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (selectedImageUri == null && existingImageUrl.isBlank()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text("Upload Medicine Photo", fontSize = 14.sp, color = Color.Gray)
                        }
                    } else {
                        AsyncImage(model = selectedImageUri ?: existingImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }

                HospitalTextField(value = name, onValueChange = { name = it }, label = "Medicine Name", icon = Icons.Default.Medication, enabled = !isSaving)
                HospitalTextField(value = shopName, onValueChange = { shopName = it }, label = "Shop Name", icon = Icons.Default.Store, enabled = !isSaving)
                HospitalTextField(value = shopAddress, onValueChange = { shopAddress = it }, label = "Shop Address", icon = Icons.Default.LocationOn, enabled = !isSaving)
                HospitalTextField(value = price, onValueChange = { price = it }, label = "Price (e.g. ₹100)", icon = Icons.Default.Payments, enabled = !isSaving)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && shopName.isNotBlank() && price.isNotBlank()) {
                            isSaving = true
                            val medData = Medicine(id = medicineId ?: "", name = name, shopName = shopName, shopAddress = shopAddress, price = price, imageUrl = selectedImageUri?.toString() ?: existingImageUrl)
                            val collection = db.collection("medicines_store")
                            
                            if (medicineId != null) {
                                collection.document(medicineId).set(medData).addOnSuccessListener {
                                    Toast.makeText(context.applicationContext, "Medicine Updated Successfully", Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                    onBack()
                                }.addOnFailureListener { isSaving = false; Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show() }
                            } else {
                                val newDocRef = collection.document()
                                val dataWithId = medData.copy(id = newDocRef.id)
                                newDocRef.set(dataWithId).addOnSuccessListener {
                                    Toast.makeText(context.applicationContext, "Medicine Added Successfully", Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                    onBack()
                                }.addOnFailureListener { isSaving = false; Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show() }
                            }
                        } else { Toast.makeText(context, "Name, Shop and Price are required", Toast.LENGTH_SHORT).show() }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) { 
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (medicineId != null) "Update Medicine" else "Add Medicine", fontSize = 16.sp, fontWeight = FontWeight.Bold) 
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            if (isSaving) {
                Box(modifier = Modifier.fillMaxSize().clickable(enabled = true, onClick = {}))
            }
        }
    }
}
