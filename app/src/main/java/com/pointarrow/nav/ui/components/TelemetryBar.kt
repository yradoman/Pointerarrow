package com.pointarrow.nav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointarrow.nav.ui.main.HeadingSource
import com.pointarrow.nav.ui.theme.DarkSurface
import com.pointarrow.nav.ui.theme.NeonGreen
import com.pointarrow.nav.ui.theme.TextPrimary
import com.pointarrow.nav.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TelemetryBar(
    speedKmh: Float,
    currentAltitudeMeters: Double?,
    gpsAccuracyMeters: Float?,
    headingSource: HeadingSource,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TelemetryItem(
            label = "ШВИДКІСТЬ",
            value = String.format(Locale.US, "%.1f", speedKmh),
            unit = "км/г",
            valueColor = if (speedKmh > 3.3f) NeonGreen else TextPrimary
        )

        TelemetryItem(
            label = "ВИСОТА",
            value = currentAltitudeMeters?.let { String.format(Locale.US, "%.0f", it) } ?: "--",
            unit = "м"
        )

        TelemetryItem(
            label = "ТОЧНІСТЬ",
            value = gpsAccuracyMeters?.let { String.format(Locale.US, "±%.0f", it) } ?: "--",
            unit = "м",
            valueColor = when {
                gpsAccuracyMeters == null -> TextSecondary
                gpsAccuracyMeters <= 10f -> NeonGreen
                gpsAccuracyMeters <= 30f -> Color(0xFFFFB74D)
                else -> Color(0xFFFF5252)
            }
        )

        TelemetryItem(
            label = "СЕНСОР",
            value = if (headingSource == HeadingSource.GPS_COG) "GPS" else "ROT.V",
            unit = "",
            valueColor = if (headingSource == HeadingSource.GPS_COG) NeonGreen else Color(0xFFFFB74D)
        )
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    unit: String,
    valueColor: Color = TextPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontFamily = FontFamily.Monospace
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
