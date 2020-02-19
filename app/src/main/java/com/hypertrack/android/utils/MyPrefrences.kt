package com.hypertrack.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hypertrack.android.response.CheckInResponse


class MyPreferences(context: Context) {

    private var getPreferences: SharedPreferences? = null


    init {

        getPreferences = context.getSharedPreferences("hyper_track_pref", Context.MODE_PRIVATE)

    }
    /* Get String value from the Local Preferences*/

    /* Get Int value from the Local Preferences*/

    fun saveDriverDetail(value: String) {
        getPreferences?.edit()?.putString("driver_detail", value)?.apply()
    }

    fun getDriverValue(): CheckInResponse {
        val getDetail = getPreferences?.getString("driver_detail", "")!!
        if(getDetail.isEmpty())
            return CheckInResponse()

        return Gson().fromJson(getDetail,CheckInResponse::class.java)
    }

    // Clear ALl Preferences values
    fun clearPreferences() {
        getPreferences?.edit()?.clear()?.apply()
    }


}