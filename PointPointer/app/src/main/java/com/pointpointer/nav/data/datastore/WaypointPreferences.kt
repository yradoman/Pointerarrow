package com.pointpointer.nav.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pointpointer.nav.data.model.Waypoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "nav_waypoints")

class WaypointPreferences(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    companion object {
        private val KEY_WAYPOINTS_JSON = stringPreferencesKey("waypoints_json")
        private val KEY_ACTIVE_WAYPOINT_ID = stringPreferencesKey("active_waypoint_id")
    }

    val waypointsFlow: Flow<List<Waypoint>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_WAYPOINTS_JSON] ?: return@map emptyList()
        try {
            json.decodeFromString<List<Waypoint>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    val activeWaypointIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_WAYPOINT_ID]
    }

    suspend fun saveWaypoints(waypoints: List<Waypoint>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAYPOINTS_JSON] = json.encodeToString(waypoints)
        }
    }

    suspend fun setActiveWaypointId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) {
                prefs[KEY_ACTIVE_WAYPOINT_ID] = id
            } else {
                prefs.remove(KEY_ACTIVE_WAYPOINT_ID)
            }
        }
    }
}
