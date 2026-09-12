package com.pointpointer.nav.domain.engine

import com.pointpointer.nav.domain.filter.CircularLowPassFilter
import com.pointpointer.nav.domain.math.GeoMath
import com.pointpointer.nav.ui.main.HeadingSource

data class NavigationResult(
    val arrowAngle: Float,
    val distanceMeters: Float?,
    val headingSource: HeadingSource,
    val speedKmh: Float,
    val bearingToTarget: Float?
)

class NavigationEngine {
    private val compassFilter = CircularLowPassFilter(baseAlpha = 0.14f, deadbandDegrees = 0.5f)
    private val gpsCogFilter = CircularLowPassFilter(baseAlpha = 0.30f, deadbandDegrees = 0.8f)
    private val finalArrowFilter = CircularLowPassFilter(baseAlpha = 0.22f, deadbandDegrees = 0.4f)

    private var currentHeadingSource: HeadingSource = HeadingSource.COMPASS

    fun computeNavigation(
        currentLat: Double?,
        currentLon: Double?,
        speedMetersPerSec: Float,
        gpsBearing: Float?,
        hasGpsBearing: Boolean,
        compassAzimuth: Float,
        targetLat: Double?,
        targetLon: Double?
    ): NavigationResult {
        val speedKmh = speedMetersPerSec * 3.6f

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

        if (currentLat == null || currentLon == null || targetLat == null || targetLon == null) {
            return NavigationResult(
                arrowAngle = 0f,
                distanceMeters = null,
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
