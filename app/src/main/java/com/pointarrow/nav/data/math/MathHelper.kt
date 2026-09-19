package com.pointarrow.nav.data.math

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class RouteData(
    val distanceMeters: Float,
    val initialBearing: Float
)

/**
 * Zero-Bloat high-precision mathematical helper for PointArrow navigation:
 * - WGS-84 geodesic distance and initial bearing via Location.distanceBetween()
 * - Exponential Low-Pass Filter (alpha ≈ 0.2f) for jitter-free arrow rotation
 * - Trigonometric circular mean via atan2(sinSum, cosSum) for 0°/360° continuity
 * - Threshold gating: freezes bearing updates near destination, at low speeds, or under noisy GPS
 */
object MathHelper {

    const val ARRIVAL_DISTANCE_THRESHOLD_METERS = 8.0f
    const val MIN_SPEED_THRESHOLD_MPS = 0.8f
    const val MAX_GPS_ACCURACY_THRESHOLD_METERS = 25.0f
    const val DEFAULT_SMOOTHING_ALPHA = 0.2f

    /**
     * Exact geodesic distance and initial bearing calculation using Android's WGS-84 ellipsoid model.
     */
    fun calculateWGS84Route(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): RouteData {
        val results = FloatArray(2)
        Location.distanceBetween(startLat, startLon, destLat, destLon, results)
        val distance = results[0]
        var bearing = results[1]
        if (bearing < 0f) {
            bearing += 360f
        }
        return RouteData(
            distanceMeters = distance,
            initialBearing = bearing
        )
    }

    /**
     * Strict threshold logic to eliminate jitter, drift, and erratic rotations:
     * - Returns false if distance to target is < 8.0 meters (arrival zone)
     * - Returns false if speed is < 0.8 m/s (standing still / low movement jitter)
     * - Returns false if GPS accuracy is worse than 25.0 meters (noisy fix)
     */
    fun shouldUpdateBearing(
        distanceMeters: Float,
        speedMps: Float,
        accuracyMeters: Float
    ): Boolean {
        if (distanceMeters < ARRIVAL_DISTANCE_THRESHOLD_METERS) return false
        if (speedMps < MIN_SPEED_THRESHOLD_MPS) return false
        if (accuracyMeters > MAX_GPS_ACCURACY_THRESHOLD_METERS) return false
        return true
    }

    /**
     * Exponential Smoothing (Low-Pass Filter) with alpha ≈ 0.2f for seamless, non-jittery rotation.
     * Takes the shortest angular path across 0° / 360° to avoid sudden 360-degree flips.
     */
    fun smoothBearingLowPass(
        currentSmoothed: Float,
        targetBearing: Float,
        alpha: Float = DEFAULT_SMOOTHING_ALPHA
    ): Float {
        var diff = (targetBearing - currentSmoothed) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f

        var smoothed = currentSmoothed + (alpha * diff)
        smoothed %= 360f
        if (smoothed < 0f) smoothed += 360f
        return smoothed
    }

    /**
     * Trigonometric angle averaging via atan2(sinSum, cosSum) to prevent
     * boundary wrap-around bugs across 0° / 360°.
     */
    fun calculateCircularMean(anglesDegrees: List<Float>): Float {
        if (anglesDegrees.isEmpty()) return 0f
        var sinSum = 0.0
        var cosSum = 0.0
        for (angle in anglesDegrees) {
            val rad = Math.toRadians(angle.toDouble())
            sinSum += sin(rad)
            cosSum += cos(rad)
        }
        var meanRad = atan2(sinSum, cosSum)
        var meanDeg = Math.toDegrees(meanRad).toFloat()
        if (meanDeg < 0f) meanDeg += 360f
        return meanDeg
    }
}
