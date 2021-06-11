package com.hypertrack.android.ui.common

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.stringFromResource
import com.hypertrack.logistics.android.github.R


object NotificationUtils {

    private val notificationManager by lazy {
        MyApplication.context.getSystemService(
            AppCompatActivity.NOTIFICATION_SERVICE
        ) as NotificationManager
    }

    fun showSyncNotification(context: Context) {
        @Suppress("DEPRECATION", "suggested API isn't available before Oreo")
        val notification = (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    Notification.Builder(context, MyApplication.CHANNEL_ID)
                else
                    Notification.Builder(context).setPriority(Notification.PRIORITY_LOW)
                )
            //todo string
            .setContentText("Refreshing Visits")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
        notificationManager.notify(ProgressDialogFragment.SYNC_NOTIFICATION_ID, notification)
    }


    fun dismissSyncNotification() {
        notificationManager.cancel(ProgressDialogFragment.SYNC_NOTIFICATION_ID)
    }

    fun sendNewTripNotification(context: Context) {
        val notificationIntent = Intent(
            MyApplication.context,
            MainActivity::class.java
        )

        notificationIntent.putExtra(MainActivity.KEY_TAB, Tab.MAP as Parcelable)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val contentIntent = PendingIntent.getActivity(
            MyApplication.context,
            ProgressDialogFragment.TRIP_NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    Notification.Builder(context, MyApplication.IMPORTANT_CHANNEL_ID)
                else
                    Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH)
                )
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setContentText(R.string.notification_new_trip.stringFromResource())
            .setSmallIcon(R.drawable.ic_ht_trip)
            .build()
        notificationManager.notify(ProgressDialogFragment.TRIP_NOTIFICATION_ID, notification)
    }


}