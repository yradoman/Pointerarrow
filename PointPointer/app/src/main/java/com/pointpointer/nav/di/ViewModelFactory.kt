package com.pointpointer.nav.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pointpointer.nav.ui.main.MainViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                gpsLocationSource = container.gpsLocationSource,
                orientationSensorSource = container.orientationSensorSource,
                waypointRepository = container.waypointRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
