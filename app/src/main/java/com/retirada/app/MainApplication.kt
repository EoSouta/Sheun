package com.retirada.app

import android.app.Application
import com.retirada.app.utils.AppContainer

class MainApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
