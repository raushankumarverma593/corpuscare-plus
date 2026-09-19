package com.mr_raushan.corpuscare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Department(
    val name: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun DepartmentScreen(onBack: () -> Unit, onDepartmentClick: (String) -> Unit) {
    val departments = listOf(
        Department("Cardiology", "Heart & BP related", Icons.Default.MonitorHeart),
        Department("Orthopedics", "Bones & joints related", Icons.Default.Settings),
        Department("ENT", "Ear, Nose & Throat related", Icons.Default.Hearing),
        Department("General Medicine", "General health & common illnesses", Icons.Default.MedicalServices),
        Department("Dermatology", "Skin, hair & nail related", Icons.Default.Spa),
        Department("Neurology", "Brain & nervous system related", Icons.Default.Psychology)
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
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Select Department",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(departments) { dept ->
                DepartmentCard(dept) { onDepartmentClick(dept.name) }
            }
        }
    }
}

@Composable
fun DepartmentCard(department: Department, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = department.icon,
                    contentDescription = null,
                    tint = Color(0xFF006766),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = department.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = department.description,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
