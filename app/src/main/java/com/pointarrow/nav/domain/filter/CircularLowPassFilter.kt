package com.pointarrow.nav.domain.filter

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Циклічний фільтр низьких частот для кутів у діапазоні [0, 360).
 * Запобігає ривкам стрілки при переході через північ (0° <-> 359°).
 */
class CircularLowPassFilter(
    private val alpha: Float = 0.15f
) {
    private var smoothedSin = 0f
    private var smoothedCos = 0f
    private var isInitialized = false

    fun filter(targetAngleDegrees: Float): Float {
        val rad = Math.toRadians(targetAngleDegrees.toDouble()).toFloat()
        val currentSin = sin(rad)
        val currentCos = cos(rad)

        if (!isInitialized) {
            smoothedSin = currentSin
            smoothedCos = currentCos
            isInitialized = true
        } else {
            smoothedSin += alpha * (currentSin - smoothedSin)
            smoothedCos += alpha * (currentCos - smoothedCos)
        }

        var smoothedAngle = Math.toDegrees(atan2(smoothedSin.toDouble(), smoothedCos.toDouble())).toFloat()
        if (smoothedAngle < 0f) smoothedAngle += 360f
        return smoothedAngle
    }

    fun reset() {
        isInitialized = false
        smoothedSin = 0f
        smoothedCos = 0f
    }
}
