package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CheckupService(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val iconColor: Color
)

@Composable
fun HealthCheckupScreen(onClose: () -> Unit, onItemClick: (String) -> Unit) {
    val services = listOf(
        CheckupService("General Checkup", "Full body checkup for better health", Icons.Default.MonitorHeart, Color(0xFFE3F2FD), Color(0xFF1976D2)),
        CheckupService("Heart Checkup", "Check your heart health", Icons.Default.Bolt, Color(0xFFFFEBEE), Color(0xFFD32F2F)),
        CheckupService("Blood Test", "Get accurate results", Icons.Default.Bloodtype, Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
        CheckupService("Lungs Checkup", "Breathe better, live better", Icons.Default.Air, Color(0xFFE8F5E9), Color(0xFF388E3C)),
        CheckupService("Stomach Checkup", "Care for your digestive health", Icons.Default.Bolt, Color(0xFFFFF3E0), Color(0xFFF57C00))
    )

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
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Health Checkup Services",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(services) { service ->
                CheckupItem(service) { onItemClick(service.title) }
            }
        }
    }
}

@Composable
fun CheckupItem(service: CheckupService, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(service.iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = service.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = service.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}
