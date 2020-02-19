package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.response.CheckInResponse
import com.hypertrack.android.utils.MyApplication
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckInRepo(applicationContext: Application) {


    var changePasswordResponse: MutableLiveData<CheckInResponse>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        changePasswordResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callCheckInApi(driverId: String, jsonParams: String) {

        val request =
            RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonParams)
        val changePasswordCall = application.getApiClient().makeDriverCheckIn(driverId, request)

        changePasswordCall.enqueue(object : Callback<CheckInResponse> {

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {

                changePasswordResponse?.postValue(null)
            }

            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {

                changePasswordResponse?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<CheckInResponse> {

        return changePasswordResponse!!
    }
}