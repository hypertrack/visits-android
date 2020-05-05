package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.DeliveryStatusRepo
import com.hypertrack.android.response.Deliveries

class DeliveryStatusViewModel(application : Application) : AndroidViewModel(application) {

    var deliveryStatusRepo: DeliveryStatusRepo? = null

    var deliveryStatus: LiveData<Deliveries>? = null

    private var changeMediator: MediatorLiveData<Deliveries>? = null

    init {

        deliveryStatusRepo = DeliveryStatusRepo(application)

        changeMediator = MediatorLiveData()

        singleDriverApiResponse()
    }

    // call repo method for init API
    fun callStatusMethod(driverId : String,type : String) {

        deliveryStatusRepo?.callChangeDeliveryStatus(driverId,type)
    }

    // add response here for getting
    private fun singleDriverApiResponse() {

        deliveryStatus = deliveryStatusRepo?.getResponse()

        changeMediator?.addSource(deliveryStatus!!) {
            print("Check in repo")
        }
    }
}