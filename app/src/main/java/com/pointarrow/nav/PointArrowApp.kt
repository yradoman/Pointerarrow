package com.pointarrow.nav

import android.app.Application
import com.pointarrow.nav.di.AppContainer

class PointArrowApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
