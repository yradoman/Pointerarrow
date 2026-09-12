package com.pointpointer.nav.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddWaypointDialog(
    initialLat: Double?,
    initialLon: Double?,
    onDismiss: () -> Unit,
    onSave: (name: String, lat: Double, lon: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf(initialLat?.toString() ?: "") }
    var lonText by remember { mutableStateOf(initialLon?.toString() ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Нова точка") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Назва точки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Широта (Lat)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lonText,
                    onValueChange = { lonText = it },
                    label = { Text("Довгота (Lon)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(text = errorText!!, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val lat = latText.toDoubleOrNull()
                val lon = lonText.toDoubleOrNull()
                if (name.isBlank()) {
                    errorText = "Вкажіть назву"
                    return@TextButton
                }
                if (lat == null || lat !in -90.0..90.0) {
                    errorText = "Некоректна широта (-90..90)"
                    return@TextButton
                }
                if (lon == null || lon !in -180.0..180.0) {
                    errorText = "Некоректна довгота (-180..180)"
                    return@TextButton
                }
                onSave(name.trim(), lat, lon)
            }) {
                Text("Зберегти")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
