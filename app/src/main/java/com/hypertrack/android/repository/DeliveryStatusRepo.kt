package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.response.Deliveries
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

        TODO("Denys: Not implememnted")
    }

    fun getResponse(): MutableLiveData<Deliveries> {

        return deliveryResponse!!
    }
}