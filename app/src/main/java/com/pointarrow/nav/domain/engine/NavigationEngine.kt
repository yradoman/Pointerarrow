package com.pointarrow.nav.domain.engine

import com.pointarrow.nav.data.math.MathHelper

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
    val headingSource: HeadingSource,
    val isArrived: Boolean = false
)

class NavigationEngine(
    private val alpha: Float = MathHelper.DEFAULT_SMOOTHING_ALPHA,
    private val motionTracker: MathHelper.NonCompassMotionTracker = MathHelper.NonCompassMotionTracker()
) {
    private var smoothedArrowAngle: Float = 0f
    private var isAngleInitialized: Boolean = false

    fun computeNavigation(
        currentLat: Double?,
        currentLon: Double?,
        currentAlt: Double?,
        speedMetersPerSec: Float,
        gpsBearing: Float?,
        hasGpsBearing: Boolean,
        compassAzimuth: Float?,
        hasCompassSensor: Boolean,
        gpsAccuracyMeters: Float? = null,
        targetLat: Double?,
        targetLon: Double?,
        targetAlt: Double?,
        currentTimeMs: Long = System.currentTimeMillis()
    ): NavigationResult {
        val speedKmh = speedMetersPerSec * 3.6f

        if (currentLat == null || currentLon == null || targetLat == null || targetLon == null) {
            return NavigationResult(
                distanceMeters = null,
                deltaAltitudeMeters = null,
                arrowAngle = 0f,
                speedKmh = speedKmh,
                headingSource = HeadingSource.NONE,
                isArrived = false
            )
        }

        // 1. WGS-84 Geodesic route calculation
        val route = MathHelper.calculateWGS84Route(
            startLat = currentLat,
            startLon = currentLon,
            destLat = targetLat,
            destLon = targetLon
        )
        val distanceMeters = route.distanceMeters
        val bearingToTarget = route.initialBearing
        val isArrived = distanceMeters < MathHelper.ARRIVAL_DISTANCE_THRESHOLD_METERS

        // 2. Heading source determination with non-compass motion tracker
        val accuracy = gpsAccuracyMeters ?: 10f

        val (deviceHeading, headingSource) = when {
            hasCompassSensor && compassAzimuth != null -> {
                // Hardware magnetometer compass available
                Pair(compassAzimuth, HeadingSource.COMPASS_SENSOR)
            }
            else -> {
                // Magnetometer-less device: feed through NonCompassMotionTracker
                val motionBearing = motionTracker.updateBearing(
                    currentLat = currentLat,
                    currentLon = currentLon,
                    speedMps = speedMetersPerSec,
                    accuracyMeters = accuracy,
                    currentTimeMs = currentTimeMs,
                    rawGpsBearing = if (hasGpsBearing) gpsBearing else null
                )

                if (motionBearing != null) {
                    Pair(motionBearing, HeadingSource.GPS_BEARING)
                } else {
                    Pair(0f, HeadingSource.NONE)
                }
            }
        }

        // 3. Raw relative angle to target
        val rawArrowAngle = if (headingSource != HeadingSource.NONE && !isArrived) {
            var diff = (bearingToTarget - deviceHeading) % 360f
            if (diff < 0f) diff += 360f
            diff
        } else {
            0f
        }

        // 4. Exponential low-pass smoothing (alpha ≈ 0.2f)
        if (!isAngleInitialized) {
            smoothedArrowAngle = rawArrowAngle
            isAngleInitialized = true
        } else if (!isArrived) {
            smoothedArrowAngle = MathHelper.smoothBearingLowPass(
                currentSmoothed = smoothedArrowAngle,
                targetBearing = rawArrowAngle,
                alpha = alpha
            )
        }

        val deltaAltitude = if (currentAlt != null && targetAlt != null) {
            targetAlt - currentAlt
        } else {
            null
        }

        return NavigationResult(
            distanceMeters = distanceMeters,
            deltaAltitudeMeters = deltaAltitude,
            arrowAngle = smoothedArrowAngle,
            speedKmh = speedKmh,
            headingSource = headingSource,
            isArrived = isArrived
        )
    }

    fun reset() {
        smoothedArrowAngle = 0f
        isAngleInitialized = false
        motionTracker.reset()
    }
}
