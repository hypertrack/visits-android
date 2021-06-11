package com.hypertrack.android.messaging

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.common.NotificationUtils
import com.hypertrack.android.utils.MyApplication

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class VisitsMessagingService : FirebaseMessagingService() {

    //todo test
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        MyApplication.injector.getPushReceiver().onPushReceived(this, remoteMessage)
    }

    companion object {
        const val TAG = "VisitsMessagingService"
    }
}