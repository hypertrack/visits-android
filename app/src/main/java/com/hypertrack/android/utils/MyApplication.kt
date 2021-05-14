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

        buildNotificationChannels()
    }

    private fun buildNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                notificationManager.createNotificationChannel(this)
            }

            NotificationChannel(
                IMPORTANT_CHANNEL_ID,
                getString(R.string.notification_important_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_important_channel_description)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    companion object {
        const val TAG = "MyApplication"
        const val CHANNEL_ID = "default_notification_channel"
        const val IMPORTANT_CHANNEL_ID = "important_notification_channel"
        const val SERVICES_API_KEY = BuildConfig.SERVICES_API_KEY

        val MOCK_MODE = if (BuildConfig.DEBUG) {
            BuildConfig.MOCK_MODE
        } else {
            false
        }

        val injector: Injector = Injector

        lateinit var context: Context
    }
}

enum class Destination {
    SPLASH_SCREEN, LOGIN, DRIVER_ID_INPUT, PERMISSION_REQUEST, VISITS_MANAGEMENT, VISIT_DETAILS
}