package com.pointpointer.nav

import android.app.Application
import com.pointpointer.nav.di.AppContainer

class PointPointerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
