package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import android.app.Activity
import androidx.compose.ui.graphics.Brush
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.Manifest
import android.location.Geocoder
import com.google.accompanist.permissions.*
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    userName: String = "Pankaj Kumar",
    userAddress: String = "123, MG Road, Indore",
    isAddressSaved: Boolean = false,
    isAdmin: Boolean = false,
    hasUnreadNotifications: Boolean = false,
    onLocationDetected: (String, String) -> Unit = { _, _ -> },
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSectionClick: (String) -> Unit = {}, // New parameter for admin sections
    onAppointmentClick: () -> Unit = {},
    onAmbulanceClick: () -> Unit = {},
    onMedicineClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}, // Added reports click
    onHealthCheckupClick: () -> Unit = {},
    onXrayClick: () -> Unit = {},
    onBloodDonationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backPressCount by remember { mutableStateOf(0) }
    
    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    ).apply {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S_V2) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = permissionsToRequest
    )
    var currentCity by remember { mutableStateOf(if (!isAddressSaved) "Detecting location..." else userAddress) }

    LaunchedEffect(isAddressSaved, userAddress) {
        if (isAddressSaved) {
            currentCity = userAddress
        }
    }

    LaunchedEffect(locationPermissionState.allPermissionsGranted, isAddressSaved) {
        if (!isAddressSaved) {
            if (locationPermissionState.allPermissionsGranted) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                try {
                                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Unknown"
                                        val pin = addresses[0].postalCode ?: ""
                                        currentCity = city
                                        onLocationDetected(city, pin)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            } else {
                locationPermissionState.launchMultiplePermissionRequest()
            }
        }
    }

    BackHandler {
        if (backPressCount == 1) {
            (context as? Activity)?.finish()
        } else {
            backPressCount = 1
            Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(1500)
                backPressCount = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradient Header with statusBarsPadding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF006766), Color.White),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            HeaderSection(isAdmin, hasUnreadNotifications, onNotificationClick, onProfileClick)
            
            // Branding is now inside HeaderSection, adding a check if needed for standalone
            if (isAdmin) {
                // You can add additional branding here if needed for Admin
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            GreetingSection(userName, currentCity)
            
            if (isAdmin) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Admin Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = "Manage application data and users",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val adminSections = listOf(
                        AdminSection("Manage Hospitals", Icons.Default.LocalHospital, Color(0xFFE3F2FD), "manage_hospitals"),
                        AdminSection("Manage Clinics", Icons.Default.AddBusiness, Color(0xFFF3E5F5), "manage_clinics"),
                        AdminSection("Manage Doctors", Icons.Default.PersonSearch, Color(0xFFE8F5E9), "manage_doctors"),
                        AdminSection("Manage Medicines", Icons.Default.Medication, Color(0xFFFFF3E0), "manage_medicines"),
                        AdminSection("Manage Ambulances", Icons.Default.LocalShipping, Color(0xFFE0F2F1), "manage_ambulances")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminCard(adminSections[0]) { onSectionClick(adminSections[0].route) }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AdminCard(adminSections[1]) { onSectionClick(adminSections[1].route) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminCard(adminSections[2]) { onSectionClick(adminSections[2].route) }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AdminCard(adminSections[3]) { onSectionClick(adminSections[3].route) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminCard(adminSections[4]) { onSectionClick(adminSections[4].route) }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                ServiceCategories(onHealthCheckupClick, onXrayClick, onBloodDonationClick, onReportsClick)
                Spacer(modifier = Modifier.height(24.dp))
                AppointmentBanner(onAppointmentClick)
                Spacer(modifier = Modifier.height(24.dp))
                EmergencyServices(onAmbulanceClick, onMedicineClick)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeaderSection(isAdmin: Boolean, hasUnreadNotifications: Boolean, onNotificationClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF006766)
                )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Corpus",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Care",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFD700) 
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00BCD4)
            )
        }

        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF006766)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun GreetingSection(userName: String, userAddress: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Hello, $userName",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1C1E)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "👋", fontSize = 26.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF006766),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = userAddress,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ServiceCategories(
    onHealthCheckupClick: () -> Unit,
    onXrayClick: () -> Unit,
    onBloodDonationClick: () -> Unit,
    onReportsClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        item {
            ServiceCard("Medical\nReports", Icons.Default.PersonSearch, onReportsClick)
        }
        item {
            ServiceCard("Normal Health\nCheck-up", Icons.Default.MonitorHeart, onHealthCheckupClick)
        }
        item {
            ServiceCard("X-Ray", Icons.Default.MedicalServices, onXrayClick)
        }
        item {
            ServiceCard("Blood\nDonation", Icons.Default.Bloodtype, onBloodDonationClick)
        }
    }
}

@Composable
fun ServiceCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(120.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = Color(0xFF006766)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AppointmentBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF006766))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Book Appointment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Keep your health in check!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun EmergencyServices(onAmbulanceClick: () -> Unit, onMedicineClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Emergency Services", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
        EmergencyRow("Ambulance", "Fast emergency response", Icons.Default.LocalShipping, onAmbulanceClick)
        Spacer(modifier = Modifier.height(16.dp))
        EmergencyRow("Medicine", "Order medicines online", Icons.Default.Medication, onMedicineClick)
    }
}

@Composable
fun EmergencyRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(85.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF006766),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    isAdmin: Boolean = false,
    onHomeClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF006766),
                selectedTextColor = Color(0xFF006766),
                indicatorColor = Color(0xFFE0F2F1)
            )
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (isAdmin) {
                // Profile button for Admin in the middle
                Button(
                    onClick = onProfileClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(50.dp).padding(horizontal = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Contact Hospital for Users
                Button(
                    onClick = onContactClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(50.dp).padding(horizontal = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contact\nHospital", fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (!isAdmin) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Activity") },
                label = { Text("Activity") },
                selected = currentRoute == "activity",
                onClick = onActivityClick,
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE0F2F1),
                    selectedIconColor = Color(0xFF006766),
                    selectedTextColor = Color(0xFF006766)
                )
            )
        } else {
            // Activity button removed for admin as per request implied by "clean" and adding profile
            // Keeping navigation bar item count same for balance if needed, or removing it.
            // Let's remove it for now to keep it clean.
        }
    }
}

data class AdminSection(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun AdminCard(section: AdminSection, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(section.color, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = Color(0xFF006766),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = section.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
        }
    }
}
