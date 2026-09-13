package com.pointarrow.nav.data.model

data class TargetPoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null
)
