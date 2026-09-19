package com.mr_raushan.corpuscare

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.shadow
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import android.os.Environment

enum class BookingStep {
    FORM, PREVIEW, SUCCESS_PDF
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentReceiptScreen(
    appointment: Appointment,
    doctor: Doctor,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006766),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AppointmentReceiptContent(
                doc = doctor,
                name = appointment.patientName,
                age = appointment.patientAge,
                gender = appointment.patientGender,
                mobile = appointment.patientMobile,
                bloodGroup = appointment.patientBloodGroup,
                email = appointment.patientEmail,
                dateStr = appointment.date,
                transId = appointment.transactionId,
                onPrint = {
                    generateAndPrintPdf(
                        context = context,
                        doc = doctor,
                        name = appointment.patientName,
                        age = appointment.patientAge,
                        gender = appointment.patientGender,
                        mobile = appointment.patientMobile,
                        bloodGroup = appointment.patientBloodGroup,
                        email = appointment.patientEmail,
                        dateStr = appointment.date,
                        transId = appointment.transactionId
                    )
                },
                onDownload = {
                    generateAndPrintPdf(
                        context = context,
                        doc = doctor,
                        name = appointment.patientName,
                        age = appointment.patientAge,
                        gender = appointment.patientGender,
                        mobile = appointment.patientMobile,
                        bloodGroup = appointment.patientBloodGroup,
                        email = appointment.patientEmail,
                        dateStr = appointment.date,
                        transId = appointment.transactionId,
                        isDownloadOnly = true
                    )
                },
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(doctorId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    // States
    var currentStep by remember { mutableStateOf(BookingStep.FORM) }
    var doctor by remember { mutableStateOf<Doctor?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Form States
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Male") }
    var mobile by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("B+") }
    var email by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    
    // Payment States
    var isPaid by remember { mutableStateOf(false) }
    var transactionUid by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LaunchedEffect(doctorId) {
        if (doctorId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        // First check local mock data
        val localDoc = BookingManager.localDoctors.find { it.id == doctorId }
        if (localDoc != null) {
            doctor = localDoc
            isLoading = false
        } else {
            db.collection("doctors").document(doctorId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    doctor = doc.toObject(Doctor::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            }.addOnFailureListener { isLoading = false }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentStep == BookingStep.SUCCESS_PDF) "Appointment Receipt" else "Appointment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == BookingStep.PREVIEW) currentStep = BookingStep.FORM
                        else if (currentStep == BookingStep.SUCCESS_PDF) onBack()
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF006766),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF006766))
                }
            } else if (doctor == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Doctor details not found", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("Go Back") }
                    }
                }
            } else {
                val doc = doctor!!
                when (currentStep) {
                    BookingStep.FORM -> {
                        BookingFormContent(
                            doc = doc,
                            name = name, onNameChange = { name = it },
                            age = age, onAgeChange = { age = it },
                            gender = selectedGender, onGenderChange = { selectedGender = it },
                            mobile = mobile, onMobileChange = { mobile = it },
                            bloodGroup = bloodGroup, onBloodGroupChange = { bloodGroup = it },
                            email = email, onEmailChange = { email = it },
                            selectedDate = selectedDate,
                            onDateChange = { selectedDate = it },
                            onNext = {
                                if (name.isNotBlank() && age.isNotBlank() && mobile.length == 10) {
                                    currentStep = BookingStep.PREVIEW
                                } else {
                                    val msg = if (mobile.length != 10) "Mobile number must be 10 digits" else "Please fill all required fields"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    BookingStep.PREVIEW -> {
                        BookingPreviewContent(
                            doc = doc,
                            name = name, age = age, gender = selectedGender,
                            mobile = mobile, bloodGroup = bloodGroup, email = email,
                            dateStr = dateFormat.format(selectedDate),
                            isPaid = isPaid,
                            transactionUid = transactionUid,
                            remarks = remarks,
                            onPayClick = { showQrDialog = true },
                            onSubmit = {
                                BookingManager.addAppointment(
                                    doctorId = doc.id,
                                    doctorName = doc.name,
                                    facilityId = doc.facilityId,
                                    facilityName = doc.hospitalName,
                                    date = dateFormat.format(selectedDate),
                                    time = "10:30 AM",
                                    patientName = name,
                                    patientAge = age,
                                    patientGender = selectedGender,
                                    patientMobile = mobile,
                                    patientBloodGroup = bloodGroup,
                                    patientEmail = email,
                                    transId = transactionUid
                                )
                                Toast.makeText(context, "Appointment successfully", Toast.LENGTH_SHORT).show()
                                currentStep = BookingStep.SUCCESS_PDF
                            }
                        )
                    }
                    BookingStep.SUCCESS_PDF -> {
                        AppointmentReceiptContent(
                            doc = doc,
                            name = name, age = age, gender = selectedGender,
                            mobile = mobile, bloodGroup = bloodGroup, email = email,
                            dateStr = dateFormat.format(selectedDate),
                            transId = transactionUid,
                            onPrint = { 
                                generateAndPrintPdf(
                                    context = context,
                                    doc = doc,
                                    name = name,
                                    age = age,
                                    gender = selectedGender,
                                    mobile = mobile,
                                    bloodGroup = bloodGroup,
                                    email = email,
                                    dateStr = dateFormat.format(selectedDate),
                                    transId = transactionUid
                                )
                            },
                            onDownload = { 
                                generateAndPrintPdf(
                                    context = context,
                                    doc = doc,
                                    name = name,
                                    age = age,
                                    gender = selectedGender,
                                    mobile = mobile,
                                    bloodGroup = bloodGroup,
                                    email = email,
                                    dateStr = dateFormat.format(selectedDate),
                                    transId = transactionUid,
                                    isDownloadOnly = true
                                )
                            },
                            onBack = onBack
                        )
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Scan to Pay", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.size(200.dp).background(Color.White).border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(180.dp), tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("UPI ID: corpuscare@plus.upi", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isPaid = true
                            transactionUid = "TXN${System.currentTimeMillis().toString().takeLast(8)}"
                            remarks = "Success"
                            showQrDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                    ) {
                        Text("Close & Complete Payment")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingFormContent(
    doc: Doctor,
    name: String, onNameChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    mobile: String, onMobileChange: (String) -> Unit,
    bloodGroup: String, onBloodGroupChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    selectedDate: Date, onDateChange: (Date) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.mipmap.app_logo),
                contentDescription = null,
                modifier = Modifier.size(55.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CorpusCare Plus", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
                Text("Complete Care for Every Body", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(55.dp)) // To keep title centered
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Appointment Page", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        // Box 1: Personal Details
        Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF006766), modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFF006766).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = age, onValueChange = onAgeChange, label = { Text("Age") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                var genderExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }, modifier = Modifier.weight(1.5f)) {
                    OutlinedTextField(value = gender, onValueChange = {}, readOnly = true, label = { Text("Gender") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g, color = Color.Black) },
                                onClick = { onGenderChange(g); genderExpanded = false }
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = mobile, 
                onValueChange = { if (it.length <= 10) onMobileChange(it) }, 
                label = { Text("Mobile Number") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(8.dp), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            var bgExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = bgExpanded, onExpandedChange = { bgExpanded = !bgExpanded }) {
                OutlinedTextField(value = bloodGroup, onValueChange = {}, readOnly = true, label = { Text("Blood Group") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bgExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                ExposedDropdownMenu(
                    expanded = bgExpanded, 
                    onDismissRequest = { bgExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach { bg ->
                        DropdownMenuItem(
                            text = { Text(bg, color = Color.Black) }, 
                            onClick = { onBloodGroupChange(bg); bgExpanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Appointment Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF006766), modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFF006766).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailItemRow(label = "Hospital", value = doc.hospitalName)
            DetailItemRow(label = "Address", value = doc.city + ", " + doc.state)
            DetailItemRow(label = "Doctor", value = doc.name)
            
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDateChange(Date(it))
                            }
                            showDatePicker = false
                        }) { Text("OK", color = Color(0xFF006766), fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black,
                            headlineContentColor = Color.Black,
                            weekdayContentColor = Color.Gray,
                            subheadContentColor = Color.Gray,
                            navigationContentColor = Color(0xFF006766),
                            yearContentColor = Color.Black,
                            disabledYearContentColor = Color.LightGray,
                            currentYearContentColor = Color(0xFF006766),
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = Color(0xFF006766),
                            dayContentColor = Color.Black,
                            disabledDayContentColor = Color.LightGray,
                            selectedDayContentColor = Color.White,
                            selectedDayContainerColor = Color(0xFF006766),
                            todayContentColor = Color(0xFF006766),
                            todayDateBorderColor = Color(0xFF006766)
                        )
                    )
                }
            }

            OutlinedTextField(
                value = dateFormat.format(selectedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Appointment Date") },
                trailingIcon = { 
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF006766))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookingPreviewContent(
    doc: Doctor,
    name: String, age: String, gender: String,
    mobile: String, bloodGroup: String, email: String,
    dateStr: String,
    isPaid: Boolean,
    transactionUid: String,
    remarks: String,
    onPayClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Review Details", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF006766))
        Text("Please verify all details before payment", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Review Card (Outline Box)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PreviewItemRow(label = "Patient Name", value = name)
            PreviewItemRow(label = "Age / Gender", value = "$age / $gender")
            PreviewItemRow(label = "Mobile Number", value = mobile)
            PreviewItemRow(label = "Blood Group", value = bloodGroup)
            if (email.isNotBlank()) PreviewItemRow(label = "Email", value = email)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
            
            PreviewItemRow(label = "Consulting Doctor", value = doc.name)
            PreviewItemRow(label = "Hospital/Clinic", value = doc.hospitalName)
            PreviewItemRow(label = "Appointment Date", value = dateStr)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Fee Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1).copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Consultation Fee", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text("₹ 500.00", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Transaction & Remarks (Side by Side Row)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text("Transaction ID / UID", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(transactionUid, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isPaid) Color.Black else Color.Gray)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Remarks", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
                        .border(1.dp, if (remarks == "Success") Color(0xFF4CAF50) else Color.LightGray, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = remarks.ifBlank { "Pending" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remarks == "Success") Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Buttons: Pay Now / Paid and Submit
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPayClick,
                enabled = !isPaid,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaid) Color(0xFFE0E0E0) else Color(0xFF006766),
                    contentColor = if (isPaid) Color.Gray else Color.White
                )
            ) {
                if (isPaid) Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                if (isPaid) Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPaid) "Paid" else "Pay Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            if (isPaid) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Submit Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AppointmentReceiptContent(
    doc: Doctor,
    name: String, age: String, gender: String,
    mobile: String, bloodGroup: String, email: String,
    dateStr: String,
    transId: String,
    onPrint: () -> Unit,
    onDownload: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)) // Light gray background to make PDF pop
    ) {
        // Main PDF Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            // Watermark Logo
            Image(
                painter = painterResource(id = R.mipmap.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center),
                alpha = 0.05f // Very faint watermark
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.mipmap.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("CorpusCare Plus", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
                        Text("Complete Care for Every Body", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 2.dp, color = Color(0xFF006766))
            Spacer(modifier = Modifier.height(10.dp))
            Text("E-APPOINTMENT RECEIPT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Receipt Details Table
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ReceiptItemRow("Booking ID", "CC${System.currentTimeMillis().toString().takeLast(6)}")
                ReceiptItemRow("Transaction ID", transId.ifBlank { "N/A" })
                ReceiptItemRow("Appointment Date", dateStr)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", fontSize = 14.sp, color = Color.Gray)
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                        Text("CONFIRMED", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("PATIENT INFORMATION", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                
                ReceiptItemRow("Full Name", name)
                ReceiptItemRow("Age / Gender", "$age / $gender")
                ReceiptItemRow("Mobile", mobile)
                ReceiptItemRow("Blood Group", bloodGroup)
                if (email.isNotBlank()) ReceiptItemRow("Email", email)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("APPOINTMENT WITH", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                
                ReceiptItemRow("Consultant", doc.name)
                ReceiptItemRow("Specialty", doc.department)
                ReceiptItemRow("Hospital", doc.hospitalName)
                ReceiptItemRow("Location", doc.city + ", " + doc.state)
                
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Please arrive 15 minutes before your scheduled time. Carry a digital or printed copy of this receipt.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Text("Thank you for choosing CorpusCare Plus Services", fontSize = 10.sp, color = Color.LightGray)
            Text("www.corpuscareplus.com", fontSize = 10.sp, color = Color(0xFF006766))
        }
    }
        
        // Footer Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrint,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006766))
                ) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp), tint = Color(0xFF006766))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print", color = Color(0xFF006766))
                }
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006766))
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp), tint = Color(0xFF006766))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", color = Color(0xFF006766))
                }
                Button(
                    onClick = {
                        onBack()
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PreviewItemRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReceiptItemRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.DarkGray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

fun generateAndPrintPdf(
    context: Context,
    doc: Doctor,
    name: String,
    age: String,
    gender: String,
    mobile: String,
    bloodGroup: String,
    email: String,
    dateStr: String,
    transId: String,
    isDownloadOnly: Boolean = false
) {
    val jobName = "CorpusCare_Receipt_${System.currentTimeMillis()}"

    // 1. Create the PDF Document
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()

    // Watermark Logo
    paint.alpha = 50 // Increased visibility for watermark
    val logoBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.app_logo)
    if (logoBitmap != null) {
        val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logoBitmap, 450, 450, true)
        canvas.drawBitmap(scaledLogo, (595f - 450f) / 2f, (842f - 450f) / 2f, paint)
    }
    paint.alpha = 255 // Reset alpha

    // Header Logo - Placing it very clearly next to the name
    val headerLogo = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.app_logo)
    if (headerLogo != null) {
        val scaledHeaderLogo = android.graphics.Bitmap.createScaledBitmap(headerLogo, 60, 60, true)
        canvas.drawBitmap(scaledHeaderLogo, 60f, 40f, paint) 
    }

    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 26f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#006766")
    canvas.drawText("CorpusCare Plus", 135f, 75f, paint) 
    
    paint.textSize = 12f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Complete Care for Every Body", 135f, 95f, paint) 
    
    paint.color = android.graphics.Color.parseColor("#006766")
    canvas.drawRect(50f, 120f, 545f, 123f, paint)

    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 16f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("E-APPOINTMENT RECEIPT", 50f, 150f, paint)

    // Content Table
    paint.textSize = 12f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.BLACK
    
    var y = 190f
    val lineSpacing = 25f

    fun drawRow(label: String, value: String) {
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        canvas.drawText(label, 50f, y, paint)
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(value, 200f, y, paint)
        y += lineSpacing
    }

    drawRow("Booking ID:", "CC${System.currentTimeMillis().toString().takeLast(6)}")
    drawRow("Transaction ID:", transId.ifBlank { "N/A" })
    drawRow("Date:", dateStr)
    drawRow("Status:", "CONFIRMED")
    
    y += 20f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#006766")
    canvas.drawText("PATIENT INFORMATION", 50f, y, paint)
    y += 15f
    canvas.drawLine(50f, y, 545f, y, paint)
    y += 25f

    drawRow("Full Name:", name)
    drawRow("Age / Gender:", "$age / $gender")
    drawRow("Mobile:", mobile)
    drawRow("Blood Group:", bloodGroup)
    if (email.isNotBlank()) drawRow("Email:", email)

    y += 20f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#006766")
    canvas.drawText("APPOINTMENT WITH", 50f, y, paint)
    y += 15f
    canvas.drawLine(50f, y, 545f, y, paint)
    y += 25f

    drawRow("Doctor:", doc.name)
    drawRow("Specialty:", doc.department)
    drawRow("Hospital:", doc.hospitalName)
    drawRow("Location:", "${doc.city}, ${doc.state}")

    y += 40f
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 10f
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Please arrive 15 minutes before your scheduled time.", 297f, y, paint)
    y += 15f
    canvas.drawText("Thank you for choosing CorpusCare Plus Services", 297f, y, paint)

    pdfDocument.finishPage(page)

    if (isDownloadOnly) {
        // DIRECT DOWNLOAD LOGIC
        try {
            val fileName = "${jobName}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            // Show Notification to open file
            showDownloadNotification(context, file)
            Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
        }
    } else {
        // PRINT LOGIC (Standard way to ensure UI doesn't freeze and uses Android system)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(old: PrintAttributes?, new: PrintAttributes, signal: CancellationSignal?, callback: LayoutResultCallback, extras: Bundle?) {
                if (signal?.isCanceled == true) { callback.onLayoutCancelled(); return }
                val info = PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(1).build()
                callback.onLayoutFinished(info, true)
            }
            override fun onWrite(pages: Array<out PageRange>?, destination: ParcelFileDescriptor, signal: CancellationSignal?, callback: WriteResultCallback) {
                try {
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) { e.printStackTrace() }
                finally { pdfDocument.close() }
            }
        }
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }
}

fun showDownloadNotification(context: Context, file: File) {
    val channelId = "appointment_downloads"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Downloads", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    // Intent to open the PDF
    val fileUri = FileProvider.getUriForFile(context, "com.mr_raushan.corpuscare.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("CorpusCare Plus")
        .setContentText("e-appointment downloaded. Tap to open.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

