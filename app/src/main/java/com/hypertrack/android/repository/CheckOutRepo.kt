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

class CheckOutRepo(applicationContext: Application) {


    var checkOutResponse: MutableLiveData<CheckInResponse>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        checkOutResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callCheckOutApi(driverId: String) {

        val changePasswordCall = application.getApiClient().makeDriverCheckOut(driverId)

        changePasswordCall.enqueue(object : Callback<CheckInResponse> {

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {

                checkOutResponse?.postValue(null)
            }

            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {

                checkOutResponse?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<CheckInResponse> {

        return checkOutResponse!!
    }
}