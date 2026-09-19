package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ActivityScreen(
    onAppointmentClick: (Appointment) -> Unit,
    onOrderClick: (Order) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appointments", "Orders")
    
    val orders = remember { mutableStateListOf<Order>() }
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).collection("orders").get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                    orders.clear()
                    orders.addAll(list.sortedByDescending { it.date })
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        // Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF006766))
                .statusBarsPadding()
                .height(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF006766),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF006766)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (selectedTab == 0) {
            // Appointments Tab
            if (BookingManager.appointments.isEmpty()) {
                EmptyHistory("No appointments booked yet")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(BookingManager.appointments) { appointment ->
                        AppointmentCard(appointment, onAppointmentClick)
                    }
                }
            }
        } else {
            // Orders Tab
            if (orders.isEmpty()) {
                EmptyHistory("No orders placed yet")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        OrderHistoryCard(order, onOrderClick)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order, onClick: (Order) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(order) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Text(text = order.date, fontSize = 12.sp, color = Color.Gray)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "${order.items.size} Items", fontSize = 14.sp, color = Color.DarkGray)
                Text(text = "₹${order.totalAmount}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF006766), fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun EmptyHistory(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color.Gray)
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment, onClick: (Appointment) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(appointment) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.doctorName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF006766),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = appointment.date, fontSize = 14.sp, color = Color.DarkGray)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = appointment.time, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
