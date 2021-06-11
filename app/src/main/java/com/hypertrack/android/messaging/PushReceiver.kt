package com.hypertrack.android.messaging

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.annotations.JsonAdapter
import com.hypertrack.android.interactors.TripsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.common.NotificationUtils
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import javax.inject.Provider

class PushReceiver(
    private val accountRepo: AccountRepository,
    private val tripsInteractorProvider: Provider<TripsInteractor>,
    private val moshi: Moshi
) {

    fun onPushReceived(context: Context, remoteMessage: RemoteMessage) {
        val data: Map<String, String> = if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data
        } else try {
            remoteMessage.notification?.body?.let {
                moshi.adapter<Map<String, String>>(
                    Types.newParameterizedType(
                        Map::class.java, String::class.java,
                        String()::class.java
                    )
                ).fromJson(it)
            }!!
        } catch (e: Exception) {
            mapOf()
        }
//        Log.d("hypertrack-verbose", "Got push ${data}")
        if (data["visits"] != null) {
            if (accountRepo.isLoggedIn) {
                NotificationUtils.sendNewTripNotification(context)
                tripsInteractorProvider.get().refreshTripsInBackground()
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this as Context, MainActivity::class.java)
        intent.action = Intent.ACTION_SYNC
        //removing this will lead to a crash
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}