package com.pointarrow.nav.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointarrow.nav.data.location.GpsLocationSource
import com.pointarrow.nav.data.model.TargetPoint
import com.pointarrow.nav.data.model.Waypoint
import com.pointarrow.nav.data.repository.TargetRepository
import com.pointarrow.nav.data.repository.WaypointRepository
import com.pointarrow.nav.data.sensor.OrientationSensorSource
import com.pointarrow.nav.domain.engine.NavigationEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main navigation ViewModel piping GPS, Orientation, and Target through MathHelper & NavigationEngine.
 * Features optimized non-compass motion tracking with 180° inversion hysteresis,
 * expanded 3-5m/s location window buffer, and low-speed bearing lock (< 1.0 m/s).
 */
open class NavigationViewModel(
    protected val targetRepository: TargetRepository,
    protected val waypointRepository: WaypointRepository,
    protected val gpsLocationSource: GpsLocationSource,
    protected val orientationSensorSource: OrientationSensorSource,
    protected val navigationEngine: NavigationEngine
) : ViewModel() {

    protected val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Stream of saved waypoints from DataStore
    val waypoints: StateFlow<List<Waypoint>> = waypointRepository.getWaypoints()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Main combined UI state flow: GPS + Sensors + Target -> WGS84 + Motion Tracker -> StateFlow
    val uiState: StateFlow<NavigationUiState> = combine(
        gpsLocationSource.locationFlow,
        orientationSensorSource.orientationFlow,
        targetRepository.targetPointFlow
    ) { gpsUpdate, sensorAzimuth, targetPoint ->
        val currentLat = gpsUpdate?.latitude
        val currentLon = gpsUpdate?.longitude
        val currentAlt = gpsUpdate?.altitude
        val speedMps = gpsUpdate?.speedMetersPerSec ?: 0f
        val gpsBearing = gpsUpdate?.bearing
        val hasGpsBearing = gpsUpdate?.hasBearing == true
        val accuracy = gpsUpdate?.accuracy

        val targetLat = targetPoint?.latitude
        val targetLon = targetPoint?.longitude
        val targetAlt = targetPoint?.altitude

        val navResult = navigationEngine.computeNavigation(
            currentLat = currentLat,
            currentLon = currentLon,
            currentAlt = currentAlt,
            speedMetersPerSec = speedMps,
            gpsBearing = gpsBearing,
            hasGpsBearing = hasGpsBearing,
            compassAzimuth = sensorAzimuth,
            hasCompassSensor = orientationSensorSource.hasCompass,
            gpsAccuracyMeters = accuracy,
            targetLat = targetLat,
            targetLon = targetLon,
            targetAlt = targetAlt,
            currentTimeMs = System.currentTimeMillis()
        )

        NavigationUiState(
            targetPointName = targetPoint?.name,
            distanceMeters = navResult.distanceMeters,
            currentAltitudeMeters = currentAlt,
            targetAltitudeMeters = targetAlt,
            deltaAltitudeMeters = navResult.deltaAltitudeMeters,
            arrowAngleDegrees = navResult.arrowAngle,
            speedKmh = navResult.speedKmh,
            gpsAccuracyMeters = accuracy,
            currentLatitude = currentLat,
            currentLongitude = currentLon,
            isGpsLocked = gpsUpdate != null && (gpsUpdate.accuracy <= 30f),
            headingSource = navResult.headingSource,
            isTrackingActive = true,
            hasCompassSensor = orientationSensorSource.hasCompass,
            isArrived = navResult.isArrived
        )
    }
    .sample(33L) // 30 FPS Render Throttling to save battery
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NavigationUiState()
    )

    fun refreshLocation() {
        gpsLocationSource.refresh()
    }

    fun setTargetPoint(name: String, lat: Double, lon: Double, alt: Double? = null) {
        viewModelScope.launch {
            val target = TargetPoint(
                name = name.trim().ifEmpty { "Ціль" },
                latitude = lat,
                longitude = lon,
                altitude = alt
            )
            targetRepository.setTarget(target)
            navigationEngine.reset()
            _userMessage.emit("Ціль '${target.name}' встановлено")
        }
    }

    fun setTargetFromCurrentLocation() {
        val currentState = uiState.value
        val lat = currentState.currentLatitude
        val lon = currentState.currentLongitude
        val alt = currentState.currentAltitudeMeters

        if (lat == null || lon == null) {
            viewModelScope.launch {
                _userMessage.emit("Немає фіксації GPS для встановлення цілі")
            }
            return
        }

        val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        setTargetPoint(name = "Точка $timeString", lat = lat, lon = lon, alt = alt)
    }

    fun saveWaypoint(name: String, lat: Double, lon: Double, alt: Double? = null) {
        viewModelScope.launch {
            val wp = Waypoint(
                name = name.trim().ifEmpty { "Точка" },
                latitude = lat,
                longitude = lon,
                altitude = alt,
                timestamp = System.currentTimeMillis()
            )
            waypointRepository.saveWaypoint(wp)
            _userMessage.emit("Точку '${wp.name}' збережено")
        }
    }

    fun deleteWaypoint(id: String) {
        viewModelScope.launch {
            waypointRepository.deleteWaypoint(id)
            _userMessage.emit("Точку видалено")
        }
    }

    fun navigateToWaypoint(waypoint: Waypoint) {
        setTargetPoint(
            name = waypoint.name,
            lat = waypoint.latitude,
            lon = waypoint.longitude,
            alt = waypoint.altitude
        )
    }

    fun clearTarget() {
        viewModelScope.launch {
            targetRepository.clearTarget()
            navigationEngine.reset()
            _userMessage.emit("Ціль скинуто")
        }
    }
}
