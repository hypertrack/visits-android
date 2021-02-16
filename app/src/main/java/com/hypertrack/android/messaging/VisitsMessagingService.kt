package com.hypertrack.android.messaging

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hypertrack.android.ui.MainActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class VisitsMessagingService: FirebaseMessagingService() {

    //todo
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage?.data?.get("visits") != null) {
            // Log.d(TAG, "Got remote message with payload ${remoteMessage.data}")
            val intent = Intent(this as Context, MainActivity::class.java)
            intent.action = Intent.ACTION_SYNC
            //todo why new task?
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    companion object {const val TAG = "VisitsMessagingService"}
}