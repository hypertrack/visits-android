package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
 import com.hypertrack.android.response.DriverDeliveries
import com.hypertrack.android.utils.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingleDriverRepo(applicationContext: Application) {


    var driverDeliveries: MutableLiveData<DriverDeliveries>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        driverDeliveries = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callSingleDriverApi(driverId: String  ) {

        val changePasswordCall = application.getApiClient().getDeliveries(driverId)

        changePasswordCall.enqueue(object : Callback<DriverDeliveries> {

            override fun onFailure(call: Call<DriverDeliveries>, t: Throwable) {

                driverDeliveries?.postValue(null)
            }

            override fun onResponse(
                call: Call<DriverDeliveries>,
                response: Response<DriverDeliveries>
            ) {

                driverDeliveries?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<DriverDeliveries> {

        return driverDeliveries!!
    }
}