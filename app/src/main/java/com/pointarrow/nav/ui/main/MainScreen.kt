package com.pointarrow.nav.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointarrow.nav.ui.components.CompassArrow
import com.pointarrow.nav.ui.components.TelemetryBar
import com.pointarrow.nav.ui.theme.DarkSurface
import com.pointarrow.nav.ui.theme.NeonGreen
import com.pointarrow.nav.ui.theme.PureBlack
import com.pointarrow.nav.ui.theme.TextPrimary
import com.pointarrow.nav.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun MainScreen(
    uiState: NavigationUiState,
    onOpenSetTarget: () -> Unit,
    onQuickSetCurrentLocation: () -> Unit,
    onClearTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Верхній бар: назва програми + статус GPS
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (uiState.isGpsLocked) NeonGreen else Color(0xFFFFB74D), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "POINT ARROW",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }

                    // Індикатор джерела курсу
                    Text(
                        text = if (uiState.headingSource == HeadingSource.GPS_COG) "GPS COG" else "COMPASS",
                        color = if (uiState.headingSource == HeadingSource.GPS_COG) NeonGreen else Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = uiState.satelliteStatusText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Центральна зона: стрілка або підказка + індикатор різниці висот
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.hasTarget) {
                    Box(
                        modifier = Modifier.size(310.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompassArrow(
                            targetAngle = uiState.arrowAngleDegrees,
                            isTargetReached = (uiState.distanceMeters ?: Float.MAX_VALUE) <= 5f,
                            arrowSize = 290.dp,
                            arrowColor = NeonGreen,
                            accentColor = Color(0xFF1B5E20)
                        )

                        // Vertical Offset Badge: показується ТІЛЬКИ якщо |Delta h| >= 10 метрів
                        if (uiState.showAltitudeIndicator) {
                            uiState.formattedAltitudeDelta?.let { deltaText ->
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = deltaText,
                                        color = NeonGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "ЦІЛЬ НЕ ВСТАНОВЛЕНО",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Введіть координати точки або збережіть поточну GPS позицію для початку орієнтування.",
                            color = Color(0xFF757575),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Блок інформації про ціль
            if (uiState.hasTarget) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = uiState.targetPointName ?: "Ціль",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val distMeters = uiState.distanceMeters
                            val distText = when {
                                distMeters == null -> "-- м"
                                distMeters >= 1000f -> String.format(Locale.US, "%.2f км", distMeters / 1000f)
                                else -> String.format(Locale.US, "%.0f м", distMeters)
                            }
                            Text(
                                text = "Відстань: $distText",
                                color = NeonGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = onClearTarget,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF222222), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Скинути ціль",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Панель телеметрії
            TelemetryBar(
                speedKmh = uiState.speedKmh,
                currentAltitudeMeters = uiState.currentAltitudeMeters,
                gpsAccuracyMeters = uiState.gpsAccuracyMeters,
                headingSource = uiState.headingSource
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Нижні кнопки керування ціллю
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onQuickSetCurrentLocation,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Поточна GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenSetTarget,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = PureBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Задати ціль", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
