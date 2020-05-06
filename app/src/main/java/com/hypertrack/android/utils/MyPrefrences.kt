package com.hypertrack.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
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

    companion object {
        const val DRIVER_KEY = "com.hypertrack.android.utils.driver"

    }

}