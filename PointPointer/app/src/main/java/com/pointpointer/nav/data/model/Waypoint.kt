package com.pointpointer.nav.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Waypoint(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
