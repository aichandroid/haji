package com.haji2027.app

import android.app.Application
import com.haji2027.app.di.AppContainer
import com.haji2027.app.di.DefaultAppContainer

class HajjApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
