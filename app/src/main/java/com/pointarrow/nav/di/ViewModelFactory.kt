package com.pointarrow.nav.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pointarrow.nav.ui.main.MainViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                targetRepository = container.targetRepository,
                gpsLocationSource = container.gpsLocationSource,
                orientationSensorSource = container.orientationSensorSource,
                navigationEngine = container.navigationEngine
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
