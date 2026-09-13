package com.pointarrow.nav.di

import android.content.Context
import com.pointarrow.nav.data.datastore.TargetPreferences
import com.pointarrow.nav.data.location.GpsLocationSource
import com.pointarrow.nav.data.repository.TargetRepository
import com.pointarrow.nav.data.sensor.OrientationSensorSource
import com.pointarrow.nav.domain.engine.NavigationEngine

class AppContainer(val context: Context) {
    val targetPreferences: TargetPreferences by lazy {
        TargetPreferences(context)
    }

    val targetRepository: TargetRepository by lazy {
        TargetRepository(targetPreferences)
    }

    val gpsLocationSource: GpsLocationSource by lazy {
        GpsLocationSource(context)
    }

    val orientationSensorSource: OrientationSensorSource by lazy {
        OrientationSensorSource(context)
    }

    val navigationEngine: NavigationEngine by lazy {
        NavigationEngine()
    }
}
