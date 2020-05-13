package com.hypertrack.android.utils

import android.app.Application
import com.google.gson.Gson
import com.hypertrack.android.AUTH_URL
import com.hypertrack.android.repository.*
import com.hypertrack.sdk.HyperTrack

class ServiceLocator(private val application: Application) {

    fun getDriverRepo() = DriverRepo(getDriver(),getMyPreferences(application))

    fun getAccountRepo() = AccountRepository(getAccountData(), getMyPreferences(application))

    private fun getAccountData(): AccountData = getMyPreferences(application).getAccountData()

    fun getAccessTokenRepository(deviceId : String, userName : String) = BasicAuthAccessTokenRepository(AUTH_URL, deviceId, userName)

    private fun getDriver(): Driver = getMyPreferences(application).getDriverValue()

    private fun getMyPreferences(application: Application): MyPreferences =
        MyPreferences(application.applicationContext, getGson())

    private fun getGson() = Gson()
    fun getHyperTrack(): HyperTrack {
        return HyperTrack.getInstance(application.applicationContext, getAccountRepo().publishableKey)
    }
}

fun Application.getServiceLocator() = ServiceLocator(this)