package com.devyouup.shutterflysample.core

import android.app.Application
import com.devyouup.shutterflysample.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class ShutterflySampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ShutterflySampleApp)
            modules(appModule)
        }
    }
}