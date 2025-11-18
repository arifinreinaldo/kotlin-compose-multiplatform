package com.example.kmpcleanarch.android

import android.app.Application
import com.example.kmpcleanarch.di.commonModule
import com.example.kmpcleanarch.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Android Application class
 * Initializes Koin dependency injection
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(platformModule, commonModule)
        }
    }
}
