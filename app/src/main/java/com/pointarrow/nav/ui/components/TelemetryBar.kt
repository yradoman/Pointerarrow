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
import com.pointarrow.nav.domain.engine.HeadingSource
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
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Швидкість руху
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ШВИДКІСТЬ",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format(Locale.US, "%.1f км/г", speedKmh),
                color = TextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Висота над рівнем моря
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ВИСОТА",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            val altText = currentAltitudeMeters?.let {
                String.format(Locale.US, "%.0f м", it)
            } ?: "-- м"
            Text(
                text = altText,
                color = TextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Джерело курсу (GPS або Магнітометр)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "КУРС",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            val (sourceText, sourceColor) = when (headingSource) {
                HeadingSource.GPS_BEARING -> Pair("GPS", NeonGreen)
                HeadingSource.COMPASS_SENSOR -> Pair("КОМПАС", Color(0xFF64B5F6))
                HeadingSource.NONE -> Pair("--", TextSecondary)
            }
            Text(
                text = sourceText,
                color = sourceColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
