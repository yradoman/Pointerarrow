package com.pointarrow.nav.ui.main

import com.pointarrow.nav.data.location.GpsLocationSource
import com.pointarrow.nav.data.repository.TargetRepository
import com.pointarrow.nav.data.repository.WaypointRepository
import com.pointarrow.nav.data.sensor.OrientationSensorSource
import com.pointarrow.nav.domain.engine.NavigationEngine

/**
 * MainViewModel for PointArrow.
 * Inherits all routing, filtering, and WGS84 logic from NavigationViewModel.
 */
class MainViewModel(
    targetRepository: TargetRepository,
    waypointRepository: WaypointRepository,
    gpsLocationSource: GpsLocationSource,
    orientationSensorSource: OrientationSensorSource,
    navigationEngine: NavigationEngine
) : NavigationViewModel(
    targetRepository = targetRepository,
    waypointRepository = waypointRepository,
    gpsLocationSource = gpsLocationSource,
    orientationSensorSource = orientationSensorSource,
    navigationEngine = navigationEngine
)
