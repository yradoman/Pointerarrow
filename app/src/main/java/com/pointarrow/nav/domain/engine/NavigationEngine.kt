package com.pointarrow.nav.domain.engine

import com.pointarrow.nav.domain.filter.CircularLowPassFilter
import com.pointarrow.nav.domain.math.GeoMath

enum class HeadingSource {
    GPS_BEARING,
    COMPASS_SENSOR,
    NONE
}

data class NavigationResult(
    val distanceMeters: Float?,
    val deltaAltitudeMeters: Double?,
    val arrowAngle: Float,
    val speedKmh: Float,
    val headingSource: HeadingSource
)

class NavigationEngine(
    private val lowPassFilter: CircularLowPassFilter = CircularLowPassFilter(alpha = 0.18f)
) {
    companion object {
        const val SPEED_THRESHOLD_MPS = 0.833f // 3.0 км/год
    }

    fun computeNavigation(
        currentLat: Double?,
        currentLon: Double?,
        currentAlt: Double?,
        speedMetersPerSec: Float,
        gpsBearing: Float?,
        hasGpsBearing: Boolean,
        compassAzimuth: Float?,
        targetLat: Double?,
        targetLon: Double?,
        targetAlt: Double?
    ): NavigationResult {
        val speedKmh = speedMetersPerSec * 3.6f

        if (currentLat == null || currentLon == null || targetLat == null || targetLon == null) {
            return NavigationResult(
                distanceMeters = null,
                deltaAltitudeMeters = null,
                arrowAngle = 0f,
                speedKmh = speedKmh,
                headingSource = HeadingSource.NONE
            )
        }

        val distanceMeters = GeoMath.calculateDistanceMeters(
            lat1 = currentLat,
            lon1 = currentLon,
            lat2 = targetLat,
            lon2 = targetLon
        )

        val bearingToTarget = GeoMath.calculateBearingDegrees(
            lat1 = currentLat,
            lon1 = currentLon,
            lat2 = targetLat,
            lon2 = targetLon
        )

        val (deviceHeading, headingSource) = if (speedMetersPerSec > SPEED_THRESHOLD_MPS && hasGpsBearing && gpsBearing != null) {
            Pair(gpsBearing, HeadingSource.GPS_BEARING)
        } else if (compassAzimuth != null) {
            Pair(compassAzimuth, HeadingSource.COMPASS_SENSOR)
        } else {
            Pair(0f, HeadingSource.NONE)
        }

        val rawArrowAngle = GeoMath.normalizeAngle360(bearingToTarget - deviceHeading)
        val filteredArrowAngle = lowPassFilter.filter(rawArrowAngle)

        val deltaAltitude = if (currentAlt != null && targetAlt != null) {
            targetAlt - currentAlt
        } else {
            null
        }

        return NavigationResult(
            distanceMeters = distanceMeters,
            deltaAltitudeMeters = deltaAltitude,
            arrowAngle = filteredArrowAngle,
            speedKmh = speedKmh,
            headingSource = headingSource
        )
    }

    fun reset() {
        lowPassFilter.reset()
    }
}
