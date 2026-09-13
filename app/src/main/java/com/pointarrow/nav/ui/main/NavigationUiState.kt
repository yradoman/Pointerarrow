package com.pointarrow.nav.ui.main

import kotlin.math.abs
import kotlin.math.roundToInt

enum class HeadingSource {
    COMPASS,
    GPS_COG
}

data class NavigationUiState(
    val targetPointName: String? = null,
    val distanceMeters: Float? = null,
    val currentAltitudeMeters: Double? = null,
    val targetAltitudeMeters: Double? = null,
    val deltaAltitudeMeters: Double? = null, // Delta h = h_цілі - h_поточна
    val arrowAngleDegrees: Float = 0f,
    val speedKmh: Float = 0f,
    val gpsAccuracyMeters: Float? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val isGpsLocked: Boolean = false,
    val headingSource: HeadingSource = HeadingSource.COMPASS,
    val isTrackingActive: Boolean = true
) {
    val hasTarget: Boolean get() = targetPointName != null

    // Показувати індикатор висоти тільки якщо |Delta h| >= 10 метрів
    val showAltitudeIndicator: Boolean
        get() = deltaAltitudeMeters != null && abs(deltaAltitudeMeters) >= 10.0

    // Формат: ▲ +X м (якщо ціль вище) або ▼ -X м (якщо ціль нижче)
    val formattedAltitudeDelta: String?
        get() {
            val delta = deltaAltitudeMeters ?: return null
            if (abs(delta) < 10.0) return null
            val rounded = abs(delta).roundToInt()
            return if (delta > 0) "▲ +$rounded м" else "▼ -$rounded м"
        }

    val satelliteStatusText: String
        get() = when {
            isGpsLocked -> "GPS зафіксовано"
            currentLatitude != null -> "Пошук супутників (низька точність)"
            else -> "Немає доступу до супутників"
        }
}
