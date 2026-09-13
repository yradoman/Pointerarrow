package com.pointarrow.nav.domain.filter

import kotlin.math.abs

/**
 * Circular Low Pass Filter з Deadband (0.45°).
 * Розраховує найкоротший кутовий шлях (без стрибків на межі 359° <-> 0°).
 */
class CircularLowPassFilter(
    private val alpha: Float = 0.20f,
    private val deadbandDegrees: Float = 0.45f
) {
    private var currentFilteredAngle: Float? = null

    fun filter(newAngleDegrees: Float): Float {
        val current = currentFilteredAngle ?: run {
            currentFilteredAngle = newAngleDegrees
            return newAngleDegrees
        }

        var delta = (newAngleDegrees - current) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        if (abs(delta) < deadbandDegrees) {
            return current
        }

        var smoothed = current + alpha * delta
        smoothed = (smoothed % 360f + 360f) % 360f
        currentFilteredAngle = smoothed
        return smoothed
    }

    fun reset() {
        currentFilteredAngle = null
    }
}
