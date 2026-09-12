package com.pointpointer.nav.ui.main

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointpointer.nav.data.location.GpsLocationSource
import com.pointpointer.nav.data.model.Waypoint
import com.pointpointer.nav.data.repository.ImportResult
import com.pointpointer.nav.data.repository.WaypointRepository
import com.pointpointer.nav.data.sensor.OrientationSensorSource
import com.pointpointer.nav.domain.engine.NavigationEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val gpsLocationSource: GpsLocationSource,
    private val orientationSensorSource: OrientationSensorSource,
    private val waypointRepository: WaypointRepository
) : ViewModel() {

    private val navigationEngine = NavigationEngine()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val waypoints: StateFlow<List<Waypoint>> = waypointRepository.waypoints
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeWaypointId: StateFlow<String?> = waypointRepository.activeWaypointId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val uiState: StateFlow<NavigationUiState> = combine(
        gpsLocationSource.locationFlow,
        orientationSensorSource.orientationFlow,
        waypointRepository.waypoints,
        waypointRepository.activeWaypointId
    ) { gpsUpdate, sensorAzimuth, points, activeId ->
        val activePoint = points.firstOrNull { it.id == activeId }

        val currentLat = gpsUpdate?.latitude
        val currentLon = gpsUpdate?.longitude
        val speedMps = gpsUpdate?.speedMetersPerSec ?: 0f
        val gpsBearing = gpsUpdate?.bearing
        val hasGpsBearing = gpsUpdate?.hasBearing == true

        val targetLat = activePoint?.latitude
        val targetLon = activePoint?.longitude

        val navResult = navigationEngine.computeNavigation(
            currentLat = currentLat,
            currentLon = currentLon,
            speedMetersPerSec = speedMps,
            gpsBearing = gpsBearing,
            hasGpsBearing = hasGpsBearing,
            compassAzimuth = sensorAzimuth,
            targetLat = targetLat,
            targetLon = targetLon
        )

        NavigationUiState(
            targetPointName = activePoint?.name,
            distanceMeters = navResult.distanceMeters,
            arrowAngleDegrees = navResult.arrowAngle,
            speedKmh = navResult.speedKmh,
            gpsAccuracyMeters = gpsUpdate?.accuracy,
            currentLatitude = currentLat,
            currentLongitude = currentLon,
            isGpsLocked = gpsUpdate != null && (gpsUpdate.accuracy <= 30f),
            headingSource = navResult.headingSource,
            isTrackingActive = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NavigationUiState()
    )

    fun selectWaypoint(id: String?) {
        viewModelScope.launch {
            waypointRepository.selectWaypoint(id)
            navigationEngine.reset()
        }
    }

    fun createWaypoint(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            val waypoint = Waypoint(
                name = name.trim(),
                latitude = lat,
                longitude = lon
            )
            waypointRepository.addWaypoint(waypoint)
            _userMessage.emit("Точку '${waypoint.name}' збережено")
        }
    }

    fun quickSaveCurrentLocation() {
        val currentState = uiState.value
        val lat = currentState.currentLatitude
        val lon = currentState.currentLongitude

        if (lat == null || lon == null) {
            viewModelScope.launch {
                _userMessage.emit("Неможливо зберегти: немає сигналу GPS")
            }
            return
        }

        val timeString = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())
        createWaypoint(name = "Точка $timeString", lat = lat, lon = lon)
    }

    fun deleteWaypoint(id: String) {
        viewModelScope.launch {
            waypointRepository.deleteWaypoint(id)
            _userMessage.emit("Точку видалено")
        }
    }

    fun exportWaypointsToUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = waypointRepository.exportToStream(outputStream)
                    result.onSuccess { count ->
                        _userMessage.emit("Успішно експортовано $count точок")
                    }.onFailure { error ->
                        _userMessage.emit("Помилка експорту: ${error.localizedMessage}")
                    }
                } ?: run {
                    _userMessage.emit("Не вдалося відкрити файл для запису")
                }
            } catch (e: Exception) {
                _userMessage.emit("Помилка: ${e.localizedMessage}")
            }
        }
    }

    fun importWaypointsFromUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    when (val result = waypointRepository.importFromStream(inputStream)) {
                        is ImportResult.Success -> {
                            _userMessage.emit("Успішно імпортовано ${result.importedCount} нових точок")
                        }
                        is ImportResult.Error -> {
                            _userMessage.emit(result.message)
                        }
                    }
                } ?: run {
                    _userMessage.emit("Не вдалося відкрити файл для читання")
                }
            } catch (e: Exception) {
                _userMessage.emit("Помилка: ${e.localizedMessage}")
            }
        }
    }
}
