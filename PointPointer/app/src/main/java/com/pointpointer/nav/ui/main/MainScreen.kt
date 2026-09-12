package com.pointpointer.nav.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointpointer.nav.ui.components.CompassArrow
import com.pointpointer.nav.ui.components.TelemetryGrid
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: NavigationUiState,
    onOpenWaypointsList: () -> Unit,
    onQuickSavePoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isGpsLocked) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                            contentDescription = "GPS Status",
                            tint = if (uiState.isGpsLocked) Color(0xFF00E676) else Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "POINT POINTER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onQuickSavePoint) {
                        Icon(
                            imageVector = Icons.Default.AddLocationAlt,
                            contentDescription = "Зберегти поточну точку",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onOpenWaypointsList) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Список точок",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TargetPointHeader(
                targetName = uiState.targetPointName,
                onSelectTargetClick = onOpenWaypointsList,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.hasTarget) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CompassArrow(
                            targetAngle = uiState.arrowAngleDegrees,
                            isTargetReached = (uiState.distanceMeters ?: Float.MAX_VALUE) <= 5f,
                            arrowSize = 290.dp,
                            arrowColor = Color(0xFF00E5FF),
                            accentColor = Color(0xFF7C4DFF)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DistanceDisplay(distanceMeters = uiState.distanceMeters)
                    }
                } else {
                    NoTargetPlaceholder(onSelectTargetClick = onOpenWaypointsList)
                }
            }

            TelemetryGrid(
                speedKmh = uiState.speedKmh,
                accuracyMeters = uiState.gpsAccuracyMeters,
                latitude = uiState.currentLatitude,
                longitude = uiState.currentLongitude,
                headingSource = uiState.headingSource
            )
        }
    }
}

@Composable
private fun TargetPointHeader(
    targetName: String?,
    onSelectTargetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF161616))
            .padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ЦІЛЬОВА ТОЧКА",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 1.5.sp
        )
        Text(
            text = targetName ?: "Точка не обрана",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (targetName != null) Color.White else Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DistanceDisplay(distanceMeters: Float?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "ВІДСТАНЬ",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 1.5.sp
        )

        val formattedDistance = when {
            distanceMeters == null -> "--"
            distanceMeters >= 1000f -> String.format(Locale.US, "%.2f км", distanceMeters / 1000f)
            else -> String.format(Locale.US, "%.0f м", distanceMeters)
        }

        Text(
            text = formattedDistance,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            ),
            color = if (distanceMeters != null && distanceMeters <= 5f) Color(0xFF00E676) else Color.White
        )
    }
}

@Composable
private fun NoTargetPlaceholder(onSelectTargetClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GpsNotFixed,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Оберіть або створіть точку",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Стрілка автоматично вкаже точний напрямок після вибору цілі",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        FilledTonalButton(onClick = onSelectTargetClick) {
            Icon(imageVector = Icons.Default.List, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Список точок")
        }
    }
}
