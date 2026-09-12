package com.pointpointer.nav.data.repository

import com.pointpointer.nav.data.datastore.WaypointPreferences
import com.pointpointer.nav.data.model.Waypoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

sealed class ImportResult {
    data class Success(val importedCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

class WaypointRepository(private val preferences: WaypointPreferences) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    val waypoints: Flow<List<Waypoint>> = preferences.waypointsFlow
    val activeWaypointId: Flow<String?> = preferences.activeWaypointIdFlow

    suspend fun addWaypoint(waypoint: Waypoint) {
        val current = preferences.waypointsFlow.first()
        preferences.saveWaypoints(current + waypoint)
        preferences.setActiveWaypointId(waypoint.id)
    }

    suspend fun deleteWaypoint(id: String) {
        val current = preferences.waypointsFlow.first()
        val activeId = preferences.activeWaypointIdFlow.first()
        preferences.saveWaypoints(current.filterNot { it.id == id })
        if (activeId == id) {
            preferences.setActiveWaypointId(null)
        }
    }

    suspend fun selectWaypoint(id: String?) {
        preferences.setActiveWaypointId(id)
    }

    suspend fun exportToStream(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val currentPoints = preferences.waypointsFlow.first()
            val jsonString = json.encodeToString(currentPoints)
            outputStream.bufferedWriter().use { writer ->
                writer.write(jsonString)
                writer.flush()
            }
            Result.success(currentPoints.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromStream(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        try {
            val rawContent = inputStream.bufferedReader().use { it.readText() }
            if (rawContent.isBlank()) {
                return@withContext ImportResult.Error("Файл порожній")
            }

            val parsedPoints = json.decodeFromString<List<Waypoint>>(rawContent)

            val validPoints = parsedPoints.filter { point ->
                point.latitude in -90.0..90.0 &&
                point.longitude in -180.0..180.0 &&
                point.name.isNotBlank()
            }

            if (validPoints.isEmpty()) {
                return@withContext ImportResult.Error("У файлі не знайдено валідних точок")
            }

            val currentPoints = preferences.waypointsFlow.first()
            val existingIds = currentPoints.map { it.id }.toSet()

            val newPoints = validPoints.filterNot { newPoint ->
                newPoint.id in existingIds ||
                currentPoints.any { it.latitude == newPoint.latitude && it.longitude == newPoint.longitude }
            }

            if (newPoints.isEmpty()) {
                return@withContext ImportResult.Error("Усі точки з файлу вже існують у списку")
            }

            preferences.saveWaypoints(currentPoints + newPoints)
            ImportResult.Success(newPoints.size)
        } catch (e: Exception) {
            ImportResult.Error("Некоректний формат JSON: ${e.localizedMessage ?: "Помилка"}")
        }
    }
}
