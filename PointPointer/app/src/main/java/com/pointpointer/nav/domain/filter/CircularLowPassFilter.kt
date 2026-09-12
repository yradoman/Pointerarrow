package com.pointpointer.nav.domain.filter

import kotlin.math.abs

class CircularLowPassFilter(
    private val baseAlpha: Float = 0.15f,
    private val deadbandDegrees: Float = 0.45f
) {
    private var currentFilteredAngle: Float? = null

    fun filter(targetAngle: Float): Float {
        val normalizedTarget = (targetAngle % 360f).let { if (it < 0f) it + 360f else it }

        val current = currentFilteredAngle ?: run {
            currentFilteredAngle = normalizedTarget
            return normalizedTarget
        }

        val delta = ((normalizedTarget - current + 540f) % 360f) - 180f

        if (abs(delta) < deadbandDegrees) {
            return current
        }

        val effectiveAlpha = if (abs(delta) > 15f) {
            (baseAlpha * 2.5f).coerceAtMost(0.50f)
        } else {
            baseAlpha
        }

        var newAngle = (current + effectiveAlpha * delta) % 360f
        if (newAngle < 0f) newAngle += 360f

        currentFilteredAngle = newAngle
        return newAngle
    }

    fun reset() {
        currentFilteredAngle = null
    }
}
