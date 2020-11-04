package com.hypertrack.android.utils

import android.app.Application
import com.hypertrack.sdk.HyperTrack

class MyApplication : Application() {

    val injector: Injector = Injector

    override fun onCreate() {
        super.onCreate()

        injector.deeplinkProcessor.appOnCreate(this)
//        HyperTrack.enableDebugLogging()
    }

    companion object { const val TAG = "MyApplication" }
}

enum class Destination {
    SPLASH_SCREEN, LOGIN, DRIVER_ID_INPUT, PERMISSION_REQUEST, VISITS_MANAGEMENT, VISIT_DETAILS
}