package com.hypertrack.android.messaging

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.common.NotificationUtils

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class VisitsMessagingService : FirebaseMessagingService() {

    //todo test
    override fun onMessageReceived(p0: RemoteMessage) {
        if (p0.data["visits"] != null) {
            if (!MainActivity.inForeground) {
//            Log.d(TAG, "Got remote message with payload ${remoteMessage.data}")
                val intent = Intent(this as Context, MainActivity::class.java)
                intent.action = Intent.ACTION_SYNC
                //removing this will lead to a crash
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                NotificationUtils.sendNewTripNotification(this)
            }
        }
    }


    companion object {
        const val TAG = "VisitsMessagingService"
    }
}