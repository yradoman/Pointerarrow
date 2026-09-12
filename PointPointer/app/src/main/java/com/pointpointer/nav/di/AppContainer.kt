package com.pointpointer.nav.di

import android.content.Context
import com.pointpointer.nav.data.datastore.WaypointPreferences
import com.pointpointer.nav.data.location.GpsLocationSource
import com.pointpointer.nav.data.repository.WaypointRepository
import com.pointpointer.nav.data.sensor.OrientationSensorSource

class AppContainer(context: Context) {
    private val preferences = WaypointPreferences(context)
    val waypointRepository = WaypointRepository(preferences)
    val gpsLocationSource = GpsLocationSource(context)
    val orientationSensorSource = OrientationSensorSource(context)
}
