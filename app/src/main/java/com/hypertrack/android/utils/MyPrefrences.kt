package com.hypertrack.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.repository.BasicAuthAccessTokenRepository
import com.hypertrack.android.response.DriverModel


class MyPreferences(context: Context, private val gson: Gson) {

    private val getPreferences : SharedPreferences
            = context.getSharedPreferences("hyper_track_pref", Context.MODE_PRIVATE)

    fun saveDriver(driverModel: DriverModel) {
        val serializedModel = gson.toJson(driverModel)
        getPreferences.edit()?.putString(DRIVER_KEY, serializedModel)?.apply()
    }

    fun getDriverValue(): DriverModel? {
        val driverDetails = getPreferences.getString(DRIVER_KEY, null)
        driverDetails?.let {
            return gson.fromJson(driverDetails,DriverModel::class.java)
        }
        return null

    }

    fun clearPreferences() {
        getPreferences.edit()?.clear()?.apply()
    }

    fun restoreRepository() : AccessTokenRepository? {
        getPreferences.getString(ACCESS_REPO_KEY, null)?.let {
            try {
                return gson.fromJson(it, BasicAuthAccessTokenRepository::class.java)
            } catch (ignored: Throwable) {

            }
        }
        return null
    }

    fun persistRepository(repo: AccessTokenRepository) {
        getPreferences.edit()?.putString(ACCESS_REPO_KEY, gson.toJson(repo))?.apply()
    }

    companion object {
        const val DRIVER_KEY = "com.hypertrack.android.utils.driver"
        val ACCESS_REPO_KEY = "com.hypertrack.android.utils.access_token_repo"

    }

}