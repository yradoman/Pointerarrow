package com.pointarrow.nav.domain.engine

import com.pointarrow.nav.domain.filter.CircularLowPassFilter
import com.pointarrow.nav.domain.math.GeoMath
import com.pointarrow.nav.ui.main.HeadingSource

data class NavigationResult(
    val arrowAngle: Float,
    val distanceMeters: Float?,
    val deltaAltitudeMeters: Double?,
    val headingSource: HeadingSource,
    val speedKmh: Float,
    val bearingToTarget: Float?
)

class NavigationEngine {
    private val compassFilter = CircularLowPassFilter(alpha = 0.22f, deadbandDegrees = 0.45f)
    private val gpsCogFilter = CircularLowPassFilter(alpha = 0.35f, deadbandDegrees = 0.45f)
    private val finalArrowFilter = CircularLowPassFilter(alpha = 0.25f, deadbandDegrees = 0.45f)

    private var currentHeadingSource = HeadingSource.COMPASS

    fun computeNavigation(
        currentLat: Double?,
        currentLon: Double?,
        currentAlt: Double?,
        speedMetersPerSec: Float,
        gpsBearing: Float?,
        hasGpsBearing: Boolean,
        compassAzimuth: Float,
        targetLat: Double?,
        targetLon: Double?,
        targetAlt: Double?
    ): NavigationResult {
        val speedKmh = speedMetersPerSec * 3.6f

        // Гістерезис 0.6 км/год:
        // Швидкість < 2.7 км/год -> Sensor.TYPE_ROTATION_VECTOR
        // Швидкість > 3.3 км/год -> GPS Course Over Ground (Location.getBearing())
        currentHeadingSource = when {
            speedKmh > 3.3f && hasGpsBearing && gpsBearing != null -> HeadingSource.GPS_COG
            speedKmh < 2.7f -> HeadingSource.COMPASS
            else -> currentHeadingSource
        }

        val deviceHeading = if (currentHeadingSource == HeadingSource.GPS_COG && gpsBearing != null) {
            gpsCogFilter.filter(gpsBearing)
        } else {
            compassFilter.filter(compassAzimuth)
        }

        // Обчислення Delta h = h_цілі - h_поточна
        val deltaAltitude = if (targetAlt != null && currentAlt != null) {
            targetAlt - currentAlt
        } else {
            null
        }

        if (currentLat == null || currentLon == null || targetLat == null || targetLon == null) {
            return NavigationResult(
                arrowAngle = 0f,
                distanceMeters = null,
                deltaAltitudeMeters = deltaAltitude,
                headingSource = currentHeadingSource,
                speedKmh = speedKmh,
                bearingToTarget = null
            )
        }

        val distance = GeoMath.calculateDistanceMeters(
            lat1 = currentLat, lon1 = currentLon,
            lat2 = targetLat, lon2 = targetLon
        )

        val bearingToTarget = GeoMath.calculateBearing(
            lat1 = currentLat, lon1 = currentLon,
            lat2 = targetLat, lon2 = targetLon
        )

        val rawArrowAngle = GeoMath.normalizeDegrees(bearingToTarget - deviceHeading)
        val smoothedArrowAngle = finalArrowFilter.filter(rawArrowAngle)

        return NavigationResult(
            arrowAngle = smoothedArrowAngle,
            distanceMeters = distance,
            deltaAltitudeMeters = deltaAltitude,
            headingSource = currentHeadingSource,
            speedKmh = speedKmh,
            bearingToTarget = bearingToTarget
        )
    }

    fun reset() {
        compassFilter.reset()
        gpsCogFilter.reset()
        finalArrowFilter.reset()
    }
}
