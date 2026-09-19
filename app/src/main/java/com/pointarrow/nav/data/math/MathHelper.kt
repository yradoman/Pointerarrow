package com.pointarrow.nav.data.math

import android.location.Location
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RouteData(
    val distanceMeters: Float,
    val initialBearing: Float
)

data class TimedLocation(
    val latitude: Double,
    val longitude: Double,
    val timeMs: Long,
    val speedMps: Float,
    val accuracy: Float
)

/**
 * Zero-Bloat high-precision mathematical helper for PointArrow navigation:
 * - WGS-84 geodesic distance and initial bearing via Location.distanceBetween()
 * - Exponential Low-Pass Filter (alpha ≈ 0.2f) for jitter-free arrow rotation
 * - Non-Compass Fallback Mechanisms:
 *   1. Low-Speed Bearing Lock: freezes heading when speed < 1.0 m/s
 *   2. Heading Inversion Hysteresis: requires >= 4.0m displacement before accepting ~180° flip (140°-220°)
 *   3. Expanded Location Buffer: calculates motion bearing across 3-5m / 3-5s sliding window
 */
object MathHelper {

    const val ARRIVAL_DISTANCE_THRESHOLD_METERS = 8.0f
    const val LOW_SPEED_BEARING_LOCK_MPS = 1.0f
    const val MAX_GPS_ACCURACY_THRESHOLD_METERS = 25.0f
    const val DEFAULT_SMOOTHING_ALPHA = 0.2f

    // Inversion hysteresis thresholds
    const val INVERSION_MIN_ANGLE_DIFF = 140.0f
    const val INVERSION_MAX_ANGLE_DIFF = 220.0f
    const val INVERSION_SUSTAINED_DISPLACEMENT_METERS = 4.0f

    // Windowed location buffer parameters
    const val WINDOW_MIN_DISPLACEMENT_METERS = 3.5f
    const val WINDOW_MIN_TIME_SPAN_MS = 2500L
    const val WINDOW_MAX_TIME_SPAN_MS = 8000L

    /**
     * Exact geodesic distance and initial bearing calculation using Android's WGS-84 ellipsoid model.
     * Includes mathematical spherical geodesic fallback for pure JVM unit test environments.
     */
    fun calculateWGS84Route(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): RouteData {
        try {
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
        } catch (e: RuntimeException) {
            return calculateGeodesicFallback(startLat, startLon, destLat, destLon)
        }
    }

    private fun calculateGeodesicFallback(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): RouteData {
        val earthRadius = 6371000.0 // meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2.0) * sin(deltaPhi / 2.0) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2.0) * sin(deltaLambda / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        val distance = (earthRadius * c).toFloat()

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        var initialBearing = Math.toDegrees(atan2(y, x)).toFloat()
        if (initialBearing < 0f) {
            initialBearing += 360f
        }

        return RouteData(distance, initialBearing)
    }

    /**
     * Checks if angular difference between current bearing and candidate new bearing
     * represents an inversion (~180° flip in range [140°, 220°]).
     */
    fun isHeadingInversion(currentBearing: Float, newBearing: Float): Boolean {
        var delta = (newBearing - currentBearing) % 360f
        if (delta < 0f) delta += 360f
        return delta in INVERSION_MIN_ANGLE_DIFF..INVERSION_MAX_ANGLE_DIFF
    }

    /**
     * Calculates the shortest angular difference (0° to 180°) between two bearings.
     */
    fun angularDifference(bearing1: Float, bearing2: Float): Float {
        val diff = abs(bearing1 - bearing2) % 360f
        return if (diff > 180f) 360f - diff else diff
    }

    /**
     * Strict threshold logic for basic movement validity:
     * - Returns false if distance to target is < 8.0 meters (arrival zone)
     * - Returns false if speed is < 1.0 m/s (standing still / low movement jitter)
     * - Returns false if GPS accuracy is worse than 25.0 meters (noisy fix)
     */
    fun shouldUpdateBearing(
        distanceMeters: Float,
        speedMps: Float,
        accuracyMeters: Float
    ): Boolean {
        if (distanceMeters < ARRIVAL_DISTANCE_THRESHOLD_METERS) return false
        if (speedMps < LOW_SPEED_BEARING_LOCK_MPS) return false
        if (accuracyMeters > MAX_GPS_ACCURACY_THRESHOLD_METERS) return false
        return true
    }

    /**
     * Exponential Smoothing (Low-Pass Filter) with alpha ≈ 0.2f for seamless rotation.
     * Takes the shortest angular path across 0° / 360° to avoid sudden full flips.
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
     * Trigonometric circular averaging via atan2(sinSum, cosSum)
     * to eliminate phase wrapping discontinuity at 0° / 360°.
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

    /**
     * Specialized motion bearing tracker for magnetometer-less devices:
     * - Accumulates a sliding location buffer (3-5 seconds or 3.5-5 meters prior)
     * - Freezes bearing when speed drops below 1.0 m/s
     * - Enforces 4.0m sustained displacement hysteresis before accepting 180° inversions
     */
    class NonCompassMotionTracker(
        private val lowSpeedLockMps: Float = LOW_SPEED_BEARING_LOCK_MPS,
        private val inversionDisplacementThreshold: Float = INVERSION_SUSTAINED_DISPLACEMENT_METERS,
        private val minWindowDisplacement: Float = WINDOW_MIN_DISPLACEMENT_METERS
    ) {
        private val locationBuffer = ArrayDeque<TimedLocation>()
        private var lastReliableBearing: Float? = null
        private var inversionAnchorLocation: TimedLocation? = null
        private var pendingInversionBearing: Float? = null

        fun updateBearing(
            currentLat: Double,
            currentLon: Double,
            speedMps: Float,
            accuracyMeters: Float,
            currentTimeMs: Long = System.currentTimeMillis(),
            rawGpsBearing: Float? = null
        ): Float? {
            // 1. Low-Speed Bearing Lock & Accuracy Gate
            // Freeze arrow bearing if speed < 1.0 m/s or GPS accuracy is poor (>25m)
            if (speedMps < lowSpeedLockMps || accuracyMeters > MAX_GPS_ACCURACY_THRESHOLD_METERS) {
                return lastReliableBearing
            }

            val currentLocation = TimedLocation(
                latitude = currentLat,
                longitude = currentLon,
                timeMs = currentTimeMs,
                speedMps = speedMps,
                accuracy = accuracyMeters
            )

            locationBuffer.addLast(currentLocation)

            // Prune expired locations (older than 10 seconds)
            while (locationBuffer.isNotEmpty() && (currentTimeMs - locationBuffer.first().timeMs) > 10000L) {
                locationBuffer.removeFirst()
            }

            // 2. Expanded Location Buffer Window: find anchor 3-5s or >=3.5m prior
            var referenceLocation: TimedLocation? = null
            for (idx in (locationBuffer.size - 1) downTo 0) {
                val candidate = locationBuffer[idx]
                val timeDiff = currentTimeMs - candidate.timeMs
                val route = calculateWGS84Route(
                    startLat = candidate.latitude,
                    startLon = candidate.longitude,
                    destLat = currentLat,
                    destLon = currentLon
                )

                if (route.distanceMeters >= minWindowDisplacement || timeDiff >= WINDOW_MIN_TIME_SPAN_MS) {
                    if (route.distanceMeters >= 2.0f) {
                        referenceLocation = candidate
                        break
                    }
                }
            }

            var candidateBearing: Float? = null
            if (referenceLocation != null) {
                val route = calculateWGS84Route(
                    startLat = referenceLocation.latitude,
                    startLon = referenceLocation.longitude,
                    destLat = currentLat,
                    destLon = currentLon
                )
                candidateBearing = route.initialBearing
            } else if (rawGpsBearing != null && speedMps >= lowSpeedLockMps) {
                // Fallback to hardware chip bearing while buffer distance is accumulating
                candidateBearing = rawGpsBearing
            }

            if (candidateBearing == null) {
                return lastReliableBearing
            }

            // First initialization
            val prevBearing = lastReliableBearing
            if (prevBearing == null) {
                lastReliableBearing = candidateBearing
                return lastReliableBearing
            }

            // 3. Heading Inversion Hysteresis (140° - 220°)
            val isInversion = isHeadingInversion(currentBearing = prevBearing, newBearing = candidateBearing)

            if (isInversion) {
                val anchor = inversionAnchorLocation
                if (anchor == null) {
                    // Enter pending inversion state: record anchor point, keep old bearing locked!
                    inversionAnchorLocation = currentLocation
                    pendingInversionBearing = candidateBearing
                    return lastReliableBearing
                } else {
                    // Check sustained displacement since inversion started
                    val displacement = calculateWGS84Route(
                        startLat = anchor.latitude,
                        startLon = anchor.longitude,
                        destLat = currentLat,
                        destLon = currentLon
                    ).distanceMeters

                    if (displacement >= inversionDisplacementThreshold) {
                        // Sustained displacement >= 4.0m confirmed in new direction! Accept inversion
                        lastReliableBearing = candidateBearing
                        inversionAnchorLocation = null
                        pendingInversionBearing = null
                        return lastReliableBearing
                    } else {
                        // Sustained displacement not reached yet: KEEP OLD BEARING LOCKED
                        return lastReliableBearing
                    }
                }
            } else {
                // Regular directional change: reset any pending inversion state
                inversionAnchorLocation = null
                pendingInversionBearing = null
                lastReliableBearing = candidateBearing
                return lastReliableBearing
            }
        }

        fun getLastReliableBearing(): Float? = lastReliableBearing

        fun reset() {
            locationBuffer.clear()
            lastReliableBearing = null
            inversionAnchorLocation = null
            pendingInversionBearing = null
        }
    }
}
