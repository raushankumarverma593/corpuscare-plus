package com.mr_raushan.corpuscare

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    // Firestore setup
    LaunchedEffect(Unit) {
        val settings = com.google.firebase.firestore.firestoreSettings {
            setPersistenceEnabled(true)
        }
        db.firestoreSettings = settings
    }
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // User State
    var userName by remember { mutableStateOf("User") }
    var userPhone by remember { mutableStateOf("") }
    var adminIdState by remember { mutableStateOf("") }
    var facilityIdState by remember { mutableStateOf("") } // Added facility state
    var userAge by remember { mutableStateOf("") }
    var userBloodGroup by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var hasUnreadNotifications by remember { mutableStateOf(false) }
    var userPincode by remember { mutableStateOf("") }
    var userCity by remember { mutableStateOf("") }
    
    val userAddresses = remember { mutableStateListOf<UserAddress>() }

    // Save Data to Firestore
    fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "name" to userName,
            "phone" to userPhone,
            "age" to userAge,
            "bloodGroup" to userBloodGroup,
            "email" to userEmail,
            "addresses" to userAddresses.map { mapOf("id" to it.id, "address" to it.address, "isDefault" to it.isDefault) }
        )
        db.collection("users").document(uid).set(data)
    }

    // Load Data from Firestore
    LaunchedEffect(auth.currentUser) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userName = doc.getString("name") ?: "User"
                    userPhone = doc.getString("phone") ?: ""
                    userAge = doc.getString("age") ?: ""
                    userBloodGroup = doc.getString("bloodGroup") ?: ""
                    userEmail = doc.getString("email") ?: ""
                    
                    val addrList = doc.get("addresses") as? List<Map<String, Any>>
                    if (addrList != null) {
                        userAddresses.clear()
                        addrList.forEach { m ->
                            userAddresses.add(UserAddress(
                                id = m["id"] as String,
                                address = m["address"] as String,
                                isDefault = m["isDefault"] as Boolean
                            ))
                        }
                    }
                    
                    // Load History and Cart
                    BookingManager.loadAppointmentsFromFirestore()
                    CartManager.loadCartFromFirestore()
                }
            }
        }
    }

    val defaultAddress = userAddresses.find { it.isDefault }?.address

    // Update location details from default address if available
    LaunchedEffect(defaultAddress) {
        if (!defaultAddress.isNullOrBlank()) {
            try {
                // Better Pincode Extraction
                val pinRegex = Regex("\\b\\d{6}\\b")
                val match = pinRegex.find(defaultAddress)
                if (match != null) {
                    val newPin = match.value
                    if (userPincode != newPin) {
                        userPincode = newPin
                        // Log to verify sync
                        android.util.Log.d("LocationSync", "Updated Pincode from Default Address: $newPin")
                    }
                }
                
                // City extraction
                val parts = defaultAddress.split(",")
                if (parts.size >= 2) {
                    val cityPart = parts[parts.size - 2].trim()
                    val newCity = if (cityPart.contains("-")) cityPart.split("-")[0].trim() else cityPart
                    if (userCity != newCity) {
                        userCity = newCity
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "activity" || currentRoute == "admin_profile" || currentRoute == "profile") {
                BottomNavBar(
                    currentRoute = currentRoute,
                    isAdmin = userPhone == "Admin",
                    onHomeClick = {
                        if (currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    onActivityClick = {
                        if (currentRoute != "activity") {
                            navController.navigate("activity")
                        }
                    },
                    onContactClick = {
                        if (currentRoute != "contact_hospital") {
                            navController.navigate("contact_hospital")
                        }
                    },
                    onProfileClick = {
                        if (userPhone == "Admin") {
                            navController.navigate("admin_profile")
                        } else {
                            navController.navigate("profile")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (auth.currentUser != null) "home" else "login",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { 
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = { 
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + 
                fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(
                    androidx.compose.foundation.layout.PaddingValues(
                        bottom = if (currentRoute == "home" || currentRoute == "activity" || currentRoute == "admin_profile" || currentRoute == "profile") paddingValues.calculateBottomPadding() else 0.dp
                    )
                )
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { name, phone, id ->
                        userName = name
                        userPhone = phone
                        if (phone == "Admin") {
                            adminIdState = id ?: ""
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        } else if (phone == "Facility") {
                            facilityIdState = id ?: ""
                            navController.navigate("facility_dashboard") { popUpTo("login") { inclusive = true } }
                        } else {
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    userName = userName,
                    userAddress = defaultAddress ?: "",
                    isAddressSaved = userAddresses.isNotEmpty(),
                    isAdmin = userPhone == "Admin",
                    hasUnreadNotifications = hasUnreadNotifications,
                    onLocationDetected = { city, pin ->
                        // Only auto-detect if user has no saved addresses to avoid overwriting default
                        if (userAddresses.isEmpty()) {
                            if (userCity != city) userCity = city
                            if (userPincode != pin) userPincode = pin
                        }
                    },
                    onNotificationClick = { 
                        hasUnreadNotifications = false
                        navController.navigate("notifications") 
                    },
                    onProfileClick = { 
                        if (userPhone == "Admin") {
                            navController.navigate("admin_profile")
                        } else {
                            navController.navigate("profile") 
                        }
                    },
                    onSectionClick = { route ->
                        navController.navigate(route)
                    },
                    onAppointmentClick = { navController.navigate("book_appointment") },
                    onAmbulanceClick = { navController.navigate("ambulance") },
                    onMedicineClick = { navController.navigate("medicine") },
                    onReportsClick = { navController.navigate("my_reports") },
                    onHealthCheckupClick = { navController.navigate("health_checkup") },
                    onXrayClick = { navController.navigate("xray") },
                    onBloodDonationClick = { navController.navigate("blood_donation") }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { 
                        auth.signOut()
                        BookingManager.clearData()
                        CartManager.clearCart()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    currentName = userName,
                    currentPhone = userPhone,
                    currentAge = userAge,
                    currentBloodGroup = userBloodGroup,
                    currentEmail = userEmail,
                    addresses = userAddresses,
                    onUpdateProfile = { n, a, b, e ->
                        userName = n
                        userAge = a
                        userBloodGroup = b
                        userEmail = e
                        saveUserData()
                    },
                    onAddAddress = { addr ->
                        if (userAddresses.isEmpty()) {
                            userAddresses.add(UserAddress(System.currentTimeMillis().toString(), addr, isDefault = true))
                        } else {
                            userAddresses.add(UserAddress(System.currentTimeMillis().toString(), addr))
                        }
                        saveUserData()
                    },
                    onEditAddress = { id, newAddr ->
                        val index = userAddresses.indexOfFirst { it.id == id }
                        if (index != -1) {
                            userAddresses[index] = userAddresses[index].copy(address = newAddr)
                            saveUserData()
                        }
                    },
                    onSetDefaultAddress = { id ->
                        val newList = userAddresses.map { it.copy(isDefault = it.id == id) }
                        userAddresses.clear()
                        userAddresses.addAll(newList)
                        saveUserData()
                    },
                    onDeleteAddress = { id ->
                        val wasDefault = userAddresses.find { it.id == id }?.isDefault ?: false
                        userAddresses.removeIf { it.id == id }
                        if (wasDefault && userAddresses.isNotEmpty()) {
                            userAddresses[0] = userAddresses[0].copy(isDefault = true)
                        }
                        saveUserData()
                    }
                )
            }
            composable("activity") { 
                ActivityScreen(
                    onAppointmentClick = { appointment ->
                        navController.navigate("view_receipt/${appointment.id}")
                    },
                    onOrderClick = { order ->
                        navController.navigate("order_receipt/${order.id}")
                    }
                )
            }
            composable(
                route = "view_receipt/{appointmentId}",
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                val appointment = BookingManager.appointments.find { it.id == appointmentId }
                if (appointment != null) {
                    val doctor = BookingManager.localDoctors.find { it.id == appointment.doctorId }
                    if (doctor != null) {
                        AppointmentReceiptScreen(
                            appointment = appointment,
                            doctor = doctor,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
            composable("notifications") { 
                NotificationScreen(onBack = { navController.popBackStack() }) 
            }
            composable("my_reports") {
                ReportsListScreen(onBack = { navController.popBackStack() })
            }
            composable("admin_profile") {
                val adminId = if (adminIdState.isNotBlank()) adminIdState else (auth.currentUser?.email?.substringBefore("@") ?: "admin01")
                AdminProfileScreen(
                    adminId = adminId,
                    currentName = userName,
                    onLogout = {
                        auth.signOut()
                        adminIdState = ""
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable("manage_hospitals") { 
                ManageHospitalScreen(
                    onBack = { navController.popBackStack() },
                    onAddHospitalClick = { navController.navigate("add_hospital") },
                    onEditHospitalClick = { id -> navController.navigate("edit_hospital/$id") }
                )
            }
            composable("add_hospital") {
                AddHospitalScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "edit_hospital/{hospitalId}",
                arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("hospitalId")
                AddHospitalScreen(hospitalId = id, onBack = { navController.popBackStack() })
            }
            composable("manage_clinics") { 
                ManageClinicsScreen(
                    onBack = { navController.popBackStack() },
                    onAddClinicClick = { navController.navigate("add_clinic") },
                    onEditClinicClick = { id -> navController.navigate("edit_clinic/$id") }
                )
            }
            composable("add_clinic") {
                AddClinicScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "edit_clinic/{clinicId}",
                arguments = listOf(navArgument("clinicId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("clinicId")
                AddClinicScreen(clinicId = id, onBack = { navController.popBackStack() })
            }
            composable("manage_doctors") { 
                ManageDoctorsScreen(
                    onBack = { navController.popBackStack() },
                    onAddDoctorClick = { navController.navigate("add_doctor") },
                    onEditDoctorClick = { id -> navController.navigate("edit_doctor/$id") }
                )
            }
            composable("add_doctor") {
                AddDoctorScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "edit_doctor/{doctorId}",
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("doctorId")
                AddDoctorScreen(doctorId = id, onBack = { navController.popBackStack() })
            }
            composable("manage_medicines") { 
                ManageMedicinesScreen(
                    onBack = { navController.popBackStack() },
                    onAddMedicineClick = { navController.navigate("add_medicine") },
                    onEditMedicineClick = { id -> navController.navigate("edit_medicine/$id") }
                )
            }
            composable("add_medicine") {
                AddMedicineScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "edit_medicine/{medicineId}",
                arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("medicineId")
                AddMedicineScreen(medicineId = id, onBack = { navController.popBackStack() })
            }
            composable("manage_ambulances") {
                ManageAmbulancesScreen(
                    onBack = { navController.popBackStack() },
                    onAddAmbulanceClick = { navController.navigate("add_ambulance") },
                    onEditAmbulanceClick = { id -> navController.navigate("edit_ambulance/$id") }
                )
            }
            composable("add_ambulance") {
                AddAmbulanceScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "edit_ambulance/{ambulanceId}",
                arguments = listOf(navArgument("ambulanceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("ambulanceId")
                AddAmbulanceScreen(ambulanceId = id, onBack = { navController.popBackStack() })
            }
            composable("book_appointment") {
                DepartmentScreen(
                    onBack = { navController.popBackStack() },
                    onDepartmentClick = { dept ->
                        navController.navigate("doctor_list/$dept")
                    }
                )
            }
            composable(
                route = "doctor_list/{deptName}",
                arguments = listOf(navArgument("deptName") { type = NavType.StringType })
            ) { backStackEntry ->
                val deptName = backStackEntry.arguments?.getString("deptName") ?: ""
                DoctorListScreen(
                    departmentName = deptName,
                    userPincode = userPincode,
                    onBack = { navController.popBackStack() },
                    onDoctorClick = { doctor ->
                        navController.navigate("booking/${doctor.id}")
                    }
                )
            }
            composable(
                route = "booking/{doctorId}",
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                BookingScreen(
                    doctorId = doctorId,
                    onBack = { 
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("ambulance") {
                AmbulanceScreen(onBack = { navController.popBackStack() })
            }
            composable("medicine") {
                MedicineStoreScreen(
                    onBack = { navController.popBackStack() },
                    onCartClick = { 
                        navController.navigate("cart")
                    },
                    userAddress = defaultAddress ?: "No default address"
                )
            }
            composable("cart") {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    userAddress = defaultAddress ?: "No default address",
                    savedAddresses = userAddresses.toList(),
                    onAddNewAddress = { addr ->
                        userAddresses.add(UserAddress(System.currentTimeMillis().toString(), addr))
                        saveUserData()
                    },
                    onPlaceOrder = { address, total, items ->
                        // Pass order data to preview via navigation or shared state
                        // For simplicity, we'll use a temporary state or navigation arguments
                        // But since we can't easily pass complex lists in route, 
                        // let's use a simpler approach or a singleton if needed.
                        // Let's use navigation with arguments for simple data and 
                        // assume preview handles the rest or use global state.
                        navController.navigate("order_preview/$address/$total")
                    }
                )
            }
            composable(
                route = "order_preview/{address}/{total}",
                arguments = listOf(
                    navArgument("address") { type = NavType.StringType },
                    navArgument("total") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val address = backStackEntry.arguments?.getString("address") ?: ""
                val total = backStackEntry.arguments?.getInt("total") ?: 0
                val items = CartManager.cartItems.toList() // Should be passed safely
                
                OrderPreviewScreen(
                    address = address,
                    total = total,
                    items = items,
                    onBack = { navController.popBackStack() },
                    onConfirmed = { order ->
                        // Clear cart is done inside OrderPreviewScreen
                        navController.navigate("order_receipt/${order.id}") {
                            popUpTo("home")
                        }
                    }
                )
            }
            composable(
                route = "order_receipt/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                // We need to fetch the order from Firestore or keep it in memory
                // For immediate display, we can use a temporary store or refetch
                var order by remember { mutableStateOf<Order?>(null) }
                val uid = auth.currentUser?.uid
                LaunchedEffect(orderId) {
                    if (uid != null) {
                        db.collection("users").document(uid).collection("orders").document(orderId).get()
                            .addOnSuccessListener { doc ->
                                order = doc.toObject(Order::class.java)
                            }
                    }
                }
                
                if (order != null) {
                    OrderReceiptScreen(
                        order = order!!,
                        onBack = { 
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("health_checkup") {
                HealthCheckupScreen(
                    onClose = { navController.popBackStack() },
                    onItemClick = { service ->
                        navController.navigate("facility_list/$service")
                    }
                )
            }
            composable("xray") {
                XRayScreen(
                    onClose = { navController.popBackStack() },
                    onItemClick = { service ->
                        navController.navigate("facility_list/$service")
                    }
                )
            }
            composable("blood_donation") {
                BloodDonationScreen(
                    onClose = { navController.popBackStack() },
                    onItemClick = { service ->
                        navController.navigate("facility_list/$service")
                    }
                )
            }
            composable(
                route = "facility_list/{serviceName}",
                arguments = listOf(navArgument("serviceName") { type = NavType.StringType })
            ) { backStackEntry ->
                val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
                FacilityListScreen(
                    serviceName = serviceName,
                    userPincode = userPincode,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("contact_hospital") {
                ContactHospitalScreen(onBack = { navController.popBackStack() })
            }
            composable("facility_dashboard") {
                FacilityDashboardScreen(
                    facilityId = facilityIdState,
                    facilityName = userName,
                    onLogout = {
                        auth.signOut()
                        facilityIdState = ""
                        navController.navigate("login") { popUpTo(0) }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderPage(title: String) {
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
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, fontSize = 18.sp, color = Color.Gray)
        }
    }
}
