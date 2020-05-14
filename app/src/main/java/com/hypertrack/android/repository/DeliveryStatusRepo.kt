package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.utils.MyApplication

class DeliveryStatusRepo(applicationContext: Application) {


    var deliveryResponse: MutableLiveData<Delivery>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        deliveryResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callChangeDeliveryStatus(deliveryId: String , type : String) {

        TODO("Denys: Not implememnted")
    }

    fun getResponse(): MutableLiveData<Delivery> {

        return deliveryResponse!!
    }
}