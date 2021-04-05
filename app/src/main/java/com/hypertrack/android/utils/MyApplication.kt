package com.hypertrack.android.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.libraries.places.api.Places
import com.hypertrack.logistics.android.github.BuildConfig
import com.hypertrack.logistics.android.github.R
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        context = this

        injector.deeplinkProcessor.appOnCreate(this)

        if (BuildConfig.DEBUG) {
//            HyperTrack.enableDebugLogging()
        }

        Places.initialize(
            applicationContext,
            getString(R.string.google_places_api_key),
            Locale.getDefault()
        );

        buildNotificationChannel()
    }

    private fun buildNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    companion object {
        const val TAG = "MyApplication"
        const val CHANNEL_ID = "default_notification_channel"
        const val SERVICES_API_KEY = BuildConfig.SERVICES_API_KEY


        val injector: Injector = Injector

        lateinit var context: Context
    }
}

enum class Destination {
    SPLASH_SCREEN, LOGIN, DRIVER_ID_INPUT, PERMISSION_REQUEST, VISITS_MANAGEMENT, VISIT_DETAILS
}