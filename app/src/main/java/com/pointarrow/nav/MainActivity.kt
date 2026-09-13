package com.pointarrow.nav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointarrow.nav.di.ViewModelFactory
import com.pointarrow.nav.ui.components.SetTargetDialog
import com.pointarrow.nav.ui.main.MainScreen
import com.pointarrow.nav.ui.main.MainViewModel
import com.pointarrow.nav.ui.theme.PointArrowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((application as PointArrowApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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
        hasFineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasCoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
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

    // Lifecycle-aware collection: when activity stops, flow collection pauses,
    // which triggers WhileSubscribed(5000) timeout and releases GPS & sensors.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSetTargetDialogOpen by remember { mutableStateOf(false) }

    MainScreen(
        uiState = uiState,
        onOpenSetTarget = { isSetTargetDialogOpen = true },
        onQuickSetCurrentLocation = { viewModel.setTargetFromCurrentLocation() },
        onClearTarget = { viewModel.clearTarget() }
    )

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
}
