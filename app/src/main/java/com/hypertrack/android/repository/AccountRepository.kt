package com.hypertrack.android.repository

import android.app.Application
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.hypertrack.android.AUTH_URL
import com.hypertrack.android.ui.CheckInActivity
import com.hypertrack.android.utils.MyPreferences
import com.hypertrack.sdk.HyperTrack
import java.lang.IllegalArgumentException

class AccountRepository(private val accountData : AccountData) {
    suspend fun onKeyReceived(key: String, application: Application) : Boolean {
        val sdk = HyperTrack.getInstance(application.applicationContext, key)
        val token = BasicAuthAccessTokenRepository(AUTH_URL, sdk.deviceID, key)
            .getAccessToken()
        if (token.isNotEmpty()) {
            // Save account data
            MyPreferences(application.applicationContext, Gson())
                .saveAccountData(AccountData(key, token))
            // navigate to Login
            return true
        } else {
            throw IllegalArgumentException("Publishable key provided is incorrect")
        }

    }


    val isVerifiedAccount : Boolean
      get() = accountData.lastToken != null

    val hasNoKey : Boolean
      get() = accountData.publishableKey == null
}


data class AccountData(
    @SerializedName("pub_key") val publishableKey : String? = null,
    @SerializedName("last_token") val lastToken : String? = null
)