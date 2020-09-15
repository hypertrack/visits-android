package com.hypertrack.android.utils

import android.app.Application
import com.hypertrack.sdk.HyperTrack
import io.branch.referral.Branch

class MyApplication : Application() {

    val injector: Injector = Injector
    override fun onCreate() {
        super.onCreate()

//        Branch.enableLogging()
        Branch.getAutoInstance(this)

        HyperTrack.enableDebugLogging()
    }

    companion object { const val TAG = "MyApplication" }
}

enum class Destination {
    SPLASH_SCREEN, LOGIN, PERMISSION_REQUEST, VISITS_MANAGEMENT, VISIT_DETAILS
}