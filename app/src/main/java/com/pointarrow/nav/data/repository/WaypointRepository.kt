package com.pointarrow.nav.data.repository

import com.pointarrow.nav.data.datastore.WaypointPreferences
import com.pointarrow.nav.data.model.Waypoint
import kotlinx.coroutines.flow.Flow

class WaypointRepository(private val preferences: WaypointPreferences) {

    /**
     * Потік списку збережених точок
     */
    fun getWaypoints(): Flow<List<Waypoint>> = preferences.waypointsFlow

    /**
     * Збереження нової або оновлення існуючої точки
     */
    suspend fun saveWaypoint(waypoint: Waypoint) {
        preferences.saveWaypoint(waypoint)
    }

    /**
     * Видалення точки за ідентифікатором
     */
    suspend fun deleteWaypoint(id: String) {
        preferences.deleteWaypoint(id)
    }
}
