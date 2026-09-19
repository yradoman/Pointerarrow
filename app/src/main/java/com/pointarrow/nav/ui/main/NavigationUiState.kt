package com.pointarrow.nav.ui.main

import com.pointarrow.nav.domain.engine.HeadingSource
import java.util.Locale
import kotlin.math.abs

data class NavigationUiState(
    val targetPointName: String? = null,
    val distanceMeters: Float? = null,
    val currentAltitudeMeters: Double? = null,
    val targetAltitudeMeters: Double? = null,
    val deltaAltitudeMeters: Double? = null,
    val arrowAngleDegrees: Float = 0f,
    val speedKmh: Float = 0f,
    val gpsAccuracyMeters: Float? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val isGpsLocked: Boolean = false,
    val headingSource: HeadingSource = HeadingSource.NONE,
    val isTrackingActive: Boolean = false,
    val hasCompassSensor: Boolean = true,
    val isArrived: Boolean = false
) {
    val hasTarget: Boolean get() = targetPointName != null
    val showNoCompassWarning: Boolean get() = !hasCompassSensor
    val showAltitudeIndicator: Boolean
        get() = deltaAltitudeMeters != null && abs(deltaAltitudeMeters) >= 10.0

    val formattedAltitudeDelta: String?
        get() {
            val delta = deltaAltitudeMeters ?: return null
            return if (delta >= 0) {
                String.format(Locale.US, "▲ +%.0f м", delta)
            } else {
                String.format(Locale.US, "▼ -%.0f м", abs(delta))
            }
        }

    val satelliteStatusText: String
        get() = when {
            gpsAccuracyMeters == null -> "GPS: ПОШУК СУПУТНИКІВ"
            gpsAccuracyMeters <= 5f -> "GPS: ТОЧНІСТЬ ±${gpsAccuracyMeters.toInt()}м (ІДЕАЛЬНА)"
            gpsAccuracyMeters <= 15f -> "GPS: ТОЧНІСТЬ ±${gpsAccuracyMeters.toInt()}м (ДОБРА)"
            else -> "GPS: ТОЧНІСТЬ ±${gpsAccuracyMeters.toInt()}м (СЛАБКА)"
        }
}
