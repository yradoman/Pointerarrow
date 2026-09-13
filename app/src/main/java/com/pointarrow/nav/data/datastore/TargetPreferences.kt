package com.pointarrow.nav.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pointarrow.nav.data.model.TargetPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.targetDataStore by preferencesDataStore(name = "pointarrow_target_prefs")

/**
 * Zero-Bloat DataStore Preferences:
 * Зберігає поточну активну цільову точку для навігаційної стрілки.
 */
class TargetPreferences(private val context: Context) {

    companion object {
        private val KEY_HAS_TARGET = booleanPreferencesKey("has_target")
        private val KEY_TARGET_NAME = stringPreferencesKey("target_name")
        private val KEY_TARGET_LAT = doublePreferencesKey("target_lat")
        private val KEY_TARGET_LON = doublePreferencesKey("target_lon")
        private val KEY_TARGET_ALT = doublePreferencesKey("target_alt")
    }

    val targetPointFlow: Flow<TargetPoint?> = context.targetDataStore.data.map { prefs ->
        val hasTarget = prefs[KEY_HAS_TARGET] == true
        if (!hasTarget) return@map null

        val lat = prefs[KEY_TARGET_LAT] ?: return@map null
        val lon = prefs[KEY_TARGET_LON] ?: return@map null
        val name = prefs[KEY_TARGET_NAME] ?: "Цільова точка"
        val alt = prefs[KEY_TARGET_ALT]

        TargetPoint(
            name = name,
            latitude = lat,
            longitude = lon,
            altitude = alt
        )
    }

    suspend fun saveTarget(target: TargetPoint) {
        context.targetDataStore.edit { prefs ->
            prefs[KEY_HAS_TARGET] = true
            prefs[KEY_TARGET_NAME] = target.name
            prefs[KEY_TARGET_LAT] = target.latitude
            prefs[KEY_TARGET_LON] = target.longitude
            if (target.altitude != null) {
                prefs[KEY_TARGET_ALT] = target.altitude
            } else {
                prefs.remove(KEY_TARGET_ALT)
            }
        }
    }

    suspend fun clearTarget() {
        context.targetDataStore.edit { prefs ->
            prefs[KEY_HAS_TARGET] = false
            prefs.remove(KEY_TARGET_NAME)
            prefs.remove(KEY_TARGET_LAT)
            prefs.remove(KEY_TARGET_LON)
            prefs.remove(KEY_TARGET_ALT)
        }
    }
}
