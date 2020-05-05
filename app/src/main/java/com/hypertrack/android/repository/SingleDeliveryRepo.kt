package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.utils.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingleDeliveryRepo(applicationContext: Application) {


    var singleDeliveryResponse: MutableLiveData<Deliveries>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        singleDeliveryResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callSingleDeliveryApi(deliveryId: String  ) {

        val changePasswordCall = application.getApiClient().getSingleDeliveryDetail(deliveryId)

        changePasswordCall.enqueue(object : Callback<Deliveries> {

            override fun onFailure(call: Call<Deliveries>, t: Throwable) {

                singleDeliveryResponse?.postValue(null)
            }

            override fun onResponse(
                call: Call<Deliveries>,
                response: Response<Deliveries>
            ) {

                singleDeliveryResponse?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<Deliveries> {

        return singleDeliveryResponse!!
    }
}