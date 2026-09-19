package com.mr_raushan.corpuscare

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun HospitalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label, color = Color.DarkGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = if (icon != null) { { Icon(icon, null, tint = Color(0xFF006766)) } } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF006766),
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledBorderColor = Color.LightGray.copy(alpha = 0.5f),
            disabledTextColor = Color.Black.copy(alpha = 0.5f),
            disabledLabelColor = Color.DarkGray.copy(alpha = 0.5f)
        )
    )
}
