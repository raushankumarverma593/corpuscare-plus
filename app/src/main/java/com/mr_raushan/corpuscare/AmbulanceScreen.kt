package com.mr_raushan.corpuscare

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmbulanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val ambulanceNumber = "102" // Emergency number

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
                text = "Ambulance Service",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ambulance Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFFFEBEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Emergency? Call Now",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Quick response ambulance service available 24/7 at your doorstep.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Phone Number Display
            Card(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = ambulanceNumber,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006766),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Call Button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$ambulanceNumber")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "CALL AMBULANCE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
