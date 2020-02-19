package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
 import com.hypertrack.android.response.SingleDriverResponse
import com.hypertrack.android.utils.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingleDriverRepo(applicationContext: Application) {


    var singleDriverResponse: MutableLiveData<SingleDriverResponse>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        singleDriverResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callSingleDriverApi(driverId: String  ) {

        val changePasswordCall = application.getApiClient().getSingleDriverDetail(driverId)

        changePasswordCall.enqueue(object : Callback<SingleDriverResponse> {

            override fun onFailure(call: Call<SingleDriverResponse>, t: Throwable) {

                singleDriverResponse?.postValue(null)
            }

            override fun onResponse(
                call: Call<SingleDriverResponse>,
                response: Response<SingleDriverResponse>
            ) {

                singleDriverResponse?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<SingleDriverResponse> {

        return singleDriverResponse!!
    }
}