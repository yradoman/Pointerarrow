package com.pointpointer.nav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointpointer.nav.di.ViewModelFactory
import com.pointpointer.nav.ui.components.AddWaypointDialog
import com.pointpointer.nav.ui.main.MainScreen
import com.pointpointer.nav.ui.main.MainViewModel
import com.pointpointer.nav.ui.theme.PointPointerTheme
import com.pointpointer.nav.ui.waypoints.WaypointsSheet
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((application as PointPointerApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PointPointerTheme {
                PointPointerAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PointPointerAppContent(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val waypoints by viewModel.waypoints.collectAsStateWithLifecycle()
    val activeId by viewModel.activeWaypointId.collectAsStateWithLifecycle()

    var isSheetOpen by remember { mutableStateOf(false) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { targetUri ->
        if (targetUri != null) {
            viewModel.exportWaypointsToUri(context.contentResolver, targetUri)
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { sourceUri ->
        if (sourceUri != null) {
            viewModel.importWaypointsFromUri(context.contentResolver, sourceUri)
        }
    }

    MainScreen(
        uiState = uiState,
        onOpenWaypointsList = { isSheetOpen = true },
        onQuickSavePoint = { viewModel.quickSaveCurrentLocation() }
    )

    if (isSheetOpen) {
        WaypointsSheet(
            waypoints = waypoints,
            activeWaypointId = activeId,
            onSelect = { id ->
                viewModel.selectWaypoint(id)
                isSheetOpen = false
            },
            onDelete = { id -> viewModel.deleteWaypoint(id) },
            onAddNew = { isAddDialogOpen = true },
            onExportJson = {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                exportFileLauncher.launch("waypoints_$timestamp.json")
            },
            onImportJson = {
                importFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onDismiss = { isSheetOpen = false }
        )
    }

    if (isAddDialogOpen) {
        AddWaypointDialog(
            initialLat = uiState.currentLatitude,
            initialLon = uiState.currentLongitude,
            onDismiss = { isAddDialogOpen = false },
            onSave = { name, lat, lon ->
                viewModel.createWaypoint(name, lat, lon)
                isAddDialogOpen = false
            }
        )
    }
}
