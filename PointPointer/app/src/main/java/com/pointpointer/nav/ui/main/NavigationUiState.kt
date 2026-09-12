package com.pointpointer.nav.ui.main

enum class HeadingSource {
    COMPASS,
    GPS_COG
}

data class NavigationUiState(
    val targetPointName: String? = null,
    val distanceMeters: Float? = null,
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
}
