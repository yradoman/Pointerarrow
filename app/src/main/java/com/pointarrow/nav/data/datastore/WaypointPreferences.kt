package com.pointarrow.nav.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pointarrow.nav.data.model.Waypoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.waypointsDataStore by preferencesDataStore(name = "pointarrow_waypoints_prefs")

/**
 * Збереження списку Waypoint у DataStore Preferences через вбудований Android org.json.
 * Повністю зберігає Zero-Bloat архітектуру без підключення важких сторонніх бібліотек.
 */
class WaypointPreferences(private val context: Context) {

    companion object {
        private val KEY_WAYPOINTS_JSON = stringPreferencesKey("saved_waypoints_json")
    }

    val waypointsFlow: Flow<List<Waypoint>> = context.waypointsDataStore.data.map { prefs ->
        parseWaypoints(prefs[KEY_WAYPOINTS_JSON])
    }

    suspend fun saveWaypoint(waypoint: Waypoint) {
        context.waypointsDataStore.edit { prefs ->
            val currentList = parseWaypoints(prefs[KEY_WAYPOINTS_JSON]).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == waypoint.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = waypoint
            } else {
                currentList.add(0, waypoint)
            }
            prefs[KEY_WAYPOINTS_JSON] = serializeWaypoints(currentList)
        }
    }

    suspend fun deleteWaypoint(id: String) {
        context.waypointsDataStore.edit { prefs ->
            val currentList = parseWaypoints(prefs[KEY_WAYPOINTS_JSON])
            val updatedList = currentList.filter { it.id != id }
            prefs[KEY_WAYPOINTS_JSON] = serializeWaypoints(updatedList)
        }
    }

    private fun parseWaypoints(jsonStr: String?): List<Waypoint> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Waypoint>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Waypoint(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", "Точка"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        altitude = if (obj.has("altitude") && !obj.isNull("altitude")) obj.getDouble("altitude") else null,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeWaypoints(waypoints: List<Waypoint>): String {
        val array = JSONArray()
        for (wp in waypoints) {
            val obj = JSONObject()
            obj.put("id", wp.id)
            obj.put("name", wp.name)
            obj.put("latitude", wp.latitude)
            obj.put("longitude", wp.longitude)
            if (wp.altitude != null) {
                obj.put("altitude", wp.altitude)
            } else {
                obj.put("altitude", JSONObject.NULL)
            }
            obj.put("timestamp", wp.timestamp)
            array.put(obj)
        }
        return array.toString()
    }
}
