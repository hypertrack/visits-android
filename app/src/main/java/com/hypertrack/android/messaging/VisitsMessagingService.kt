package com.hypertrack.android.messaging

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hypertrack.android.ui.VisitsManagementActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class VisitsMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage?.data?.get("visits") != null) {
            Log.d(TAG, "Got remote message with payload ${remoteMessage.data}")
            val intent = Intent(this as Context, VisitsManagementActivity::class.java)
            intent.action = Intent.ACTION_SYNC
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    companion object {const val TAG = "VisitsMessagingService"}
}