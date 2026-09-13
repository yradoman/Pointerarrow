package com.pointarrow.nav.di

import android.content.Context
import com.pointarrow.nav.data.datastore.TargetPreferences
import com.pointarrow.nav.data.datastore.WaypointPreferences
import com.pointarrow.nav.data.location.GpsLocationSource
import com.pointarrow.nav.data.repository.TargetRepository
import com.pointarrow.nav.data.repository.WaypointRepository
import com.pointarrow.nav.data.sensor.OrientationSensorSource
import com.pointarrow.nav.domain.engine.NavigationEngine

/**
 * Чистий Manual DI контейнер без Hilt / Koin / Dagger.
 * Zero-Bloat: швидка ініціалізація та відсутність рефлексії.
 */
class AppContainer(context: Context) {
    val targetPreferences = TargetPreferences(context)
    val targetRepository = TargetRepository(targetPreferences)

    val waypointPreferences = WaypointPreferences(context)
    val waypointRepository = WaypointRepository(waypointPreferences)

    val gpsLocationSource = GpsLocationSource(context)
    val orientationSensorSource = OrientationSensorSource(context)
    val navigationEngine = NavigationEngine()
}
