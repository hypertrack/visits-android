package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.utils.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckInRepo(applicationContext: Application) {


    var checkinResponse: MutableLiveData<Unit>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        checkinResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callCheckInApi(driverId: String) {

        val checkinCall = application.getApiClient().makeDriverCheckIn(driverId)

        checkinCall.enqueue(object : Callback<Unit> {

            override fun onFailure(call: Call<Unit>, t: Throwable) {

                checkinResponse?.postValue(null)
            }

            override fun onResponse(
                call: Call<Unit>,
                response: Response<Unit>
            ) {

                checkinResponse?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<Unit> {

        return checkinResponse!!
    }
}