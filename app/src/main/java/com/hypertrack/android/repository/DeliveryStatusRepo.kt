package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.response.SingleDriverResponse
import com.hypertrack.android.utils.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryStatusRepo(applicationContext: Application) {


    var deliveryResponse: MutableLiveData<Deliveries>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        deliveryResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callChangeDeliveryStatus(deliveryId: String , type : String) {

        val changePasswordCall = application.getApiClient().updateDeliveryStatusAsCompleted(deliveryId,type)

        changePasswordCall.enqueue(object : Callback<Deliveries> {

            override fun onFailure(call: Call<Deliveries>, t: Throwable) {

                deliveryResponse?.postValue(null)
            }

            override fun onResponse(
                call: Call<Deliveries>,
                response: Response<Deliveries>
            ) {

                println("Chaneg Delivery Status Success")
                deliveryResponse?.postValue(response.body())
            }
        })
    }

    fun getResponse(): MutableLiveData<Deliveries> {

        return deliveryResponse!!
    }
}