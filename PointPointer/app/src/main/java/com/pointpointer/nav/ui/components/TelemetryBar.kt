package com.pointpointer.nav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointpointer.nav.ui.main.HeadingSource
import java.util.Locale

@Composable
fun TelemetryGrid(
    speedKmh: Float,
    accuracyMeters: Float?,
    latitude: Double?,
    longitude: Double?,
    headingSource: HeadingSource,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TelemetryMetric(
                label = "ШВИДКІСТЬ",
                value = String.format(Locale.US, "%.1f", speedKmh),
                unit = "км/год"
            )

            TelemetryMetric(
                label = "ТОЧНІСТЬ GPS",
                value = accuracyMeters?.let { String.format(Locale.US, "±%.0f", it) } ?: "--",
                unit = "м",
                valueColor = when {
                    accuracyMeters == null -> Color.Gray
                    accuracyMeters <= 5f -> Color(0xFF00E676)
                    accuracyMeters <= 12f -> Color(0xFFFFD600)
                    else -> Color(0xFFFF5252)
                }
            )

            TelemetryMetric(
                label = "КУРС",
                value = if (headingSource == HeadingSource.GPS_COG) "GPS COG" else "КОМПАС",
                unit = if (headingSource == HeadingSource.GPS_COG) ">3 км/г" else "сенсор",
                valueColor = if (headingSource == HeadingSource.GPS_COG) Color(0xFF40C4FF) else Color(0xFFFFAB40)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ПОТОЧНІ КООРДИНАТИ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatCoordinates(latitude, longitude),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetric(
    label: String,
    value: String,
    unit: String,
    valueColor: Color = Color.White
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = valueColor
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

private fun formatCoordinates(lat: Double?, lon: Double?): String {
    if (lat == null || lon == null) return "Очікування сигналу GPS..."
    val latDir = if (lat >= 0) "N" else "S"
    val lonDir = if (lon >= 0) "E" else "W"
    return String.format(Locale.US, "%.5f°%s  %.5f°%s", Math.abs(lat), latDir, Math.abs(lon), lonDir)
}
