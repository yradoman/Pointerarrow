package com.pointarrow.nav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pointarrow.nav.ui.theme.DarkSurface
import com.pointarrow.nav.ui.theme.NeonGreen
import com.pointarrow.nav.ui.theme.PureBlack
import com.pointarrow.nav.ui.theme.TextPrimary
import com.pointarrow.nav.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SetTargetDialog(
    initialName: String = "Ціль",
    initialLat: Double? = null,
    initialLon: Double? = null,
    initialAlt: Double? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, lat: Double, lon: Double, alt: Double?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var latText by remember { mutableStateOf(initialLat?.let { String.format(Locale.US, "%.6f", it) } ?: "") }
    var lonText by remember { mutableStateOf(initialLon?.let { String.format(Locale.US, "%.6f", it) } ?: "") }
    var altText by remember { mutableStateOf(initialAlt?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Задати координати цілі",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = Color(0xFF333333),
                focusedLabelColor = NeonGreen,
                unfocusedLabelColor = TextSecondary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Назва точки") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = latText,
                onValueChange = { latText = it },
                label = { Text("Широта (Latitude, напр. 50.4501)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lonText,
                onValueChange = { lonText = it },
                label = { Text("Довгота (Longitude, напр. 30.5234)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = altText,
                onValueChange = { altText = it },
                label = { Text("Висота над рівнем моря (м, опціонально)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Скасувати")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        val lat = latText.replace(',', '.').toDoubleOrNull()
                        val lon = lonText.replace(',', '.').toDoubleOrNull()
                        val alt = altText.replace(',', '.').toDoubleOrNull()

                        if (lat == null || lat < -90.0 || lat > 90.0) {
                            errorMessage = "Вкажіть коректну широту від -90 до 90"
                            return@Button
                        }
                        if (lon == null || lon < -180.0 || lon > 180.0) {
                            errorMessage = "Вкажіть коректну довготу від -180 до 180"
                            return@Button
                        }

                        onSave(name.ifEmpty { "Ціль" }, lat, lon, alt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = PureBlack)
                ) {
                    Text("Зберегти", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
