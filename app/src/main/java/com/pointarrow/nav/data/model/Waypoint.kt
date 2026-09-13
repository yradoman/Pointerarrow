package com.pointarrow.nav.data.model

import java.util.UUID

/**
 * Модель збереженої навігаційної точки:
 * @param id Унікальний ідентифікатор UUID
 * @param name Назва точки
 * @param latitude Географічна широта в градусах
 * @param longitude Географічна довгота в градусах
 * @param altitude Висота над рівнем моря в метрах (якщо доступна)
 * @param timestamp Час створення точки (Unix epoch millis)
 */
data class Waypoint(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
