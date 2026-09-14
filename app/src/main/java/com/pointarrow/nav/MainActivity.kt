package com.pointarrow.nav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointarrow.nav.di.ViewModelFactory
import com.pointarrow.nav.ui.components.SaveWaypointDialog
import com.pointarrow.nav.ui.components.SetTargetDialog
import com.pointarrow.nav.ui.main.MainScreen
import com.pointarrow.nav.ui.main.MainViewModel
import com.pointarrow.nav.ui.theme.NeonGreen
import com.pointarrow.nav.ui.theme.PointArrowTheme
import com.pointarrow.nav.ui.theme.PureBlack
import com.pointarrow.nav.ui.theme.TextPrimary
import com.pointarrow.nav.ui.theme.TextSecondary
import com.pointarrow.nav.ui.waypoints.WaypointsScreen
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppNavTab {
    COMPASS,
    WAYPOINTS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((application as PointArrowApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Exception) {}

        setContent {
            PointArrowTheme {
                PointArrowAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PointArrowAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var hasFineLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCoarseLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasFineLocation = fineGranted
        hasCoarseLocation = coarseGranted
        if (fineGranted || coarseGranted) {
            viewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasFineLocation && !hasCoarseLocation) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val waypoints by viewModel.waypoints.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppNavTab.COMPASS) }
    var isSetTargetDialogOpen by remember { mutableStateOf(false) }
    var isSaveWaypointDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PureBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D0D11),
                contentColor = TextPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavTab.COMPASS,
                    onClick = { currentTab = AppNavTab.COMPASS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Компас",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Компас",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppNavTab.COMPASS) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureBlack,
                        selectedTextColor = NeonGreen,
                        indicatorColor = NeonGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.WAYPOINTS,
                    onClick = { currentTab = AppNavTab.WAYPOINTS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (waypoints.isNotEmpty()) {
                                    Badge(
                                        containerColor = NeonGreen,
                                        contentColor = PureBlack
                                    ) {
                                        Text(text = "${waypoints.size}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Точки",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Точки",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppNavTab.WAYPOINTS) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureBlack,
                        selectedTextColor = NeonGreen,
                        indicatorColor = NeonGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.COMPASS -> {
                    MainScreen(
                        uiState = uiState,
                        onOpenSetTarget = { isSetTargetDialogOpen = true },
                        onQuickSetCurrentLocation = { viewModel.setTargetFromCurrentLocation() },
                        onOpenSaveCurrentLocation = {
                            if (uiState.currentLatitude != null && uiState.currentLongitude != null) {
                                isSaveWaypointDialogOpen = true
                            } else {
                                Toast.makeText(context, "Очікування фіксації GPS для збереження точки", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClearTarget = { viewModel.clearTarget() }
                    )
                }
                AppNavTab.WAYPOINTS -> {
                    WaypointsScreen(
                        waypoints = waypoints,
                        currentTargetName = uiState.targetPointName,
                        onNavigateToWaypoint = { waypoint ->
                            viewModel.navigateToWaypoint(waypoint)
                            currentTab = AppNavTab.COMPASS
                        },
                        onDeleteWaypoint = { waypointId ->
                            viewModel.deleteWaypoint(waypointId)
                        }
                    )
                }
            }
        }
    }

    if (isSetTargetDialogOpen) {
        SetTargetDialog(
            initialName = uiState.targetPointName ?: "Ціль",
            initialLat = uiState.currentLatitude,
            initialLon = uiState.currentLongitude,
            initialAlt = uiState.currentAltitudeMeters,
            onDismiss = { isSetTargetDialogOpen = false },
            onSave = { name, lat, lon, alt ->
                viewModel.setTargetPoint(name, lat, lon, alt)
                isSetTargetDialogOpen = false
            }
        )
    }

    if (isSaveWaypointDialogOpen && uiState.currentLatitude != null && uiState.currentLongitude != null) {
        val timeString = remember {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        }
        SaveWaypointDialog(
            latitude = uiState.currentLatitude!!,
            longitude = uiState.currentLongitude!!,
            altitude = uiState.currentAltitudeMeters,
            defaultName = "Точка $timeString",
            onDismiss = { isSaveWaypointDialogOpen = false },
            onSave = { name ->
                viewModel.saveWaypoint(
                    name = name,
                    lat = uiState.currentLatitude!!,
                    lon = uiState.currentLongitude!!,
                    alt = uiState.currentAltitudeMeters
                )
                isSaveWaypointDialogOpen = false
            }
        )
    }
}
