package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.SingleDeliveryRepo
import com.hypertrack.android.response.DriverDeliveries

class SingleDeliveryViewModel(application : Application) : AndroidViewModel(application) {

    var singleDeliveryRepo: SingleDeliveryRepo? = null

    var deliveryModel: LiveData<Delivery>? = null

    private var changeMediator: MediatorLiveData<DriverDeliveries>? = null

    init {

        singleDeliveryRepo = SingleDeliveryRepo(application)

        changeMediator = MediatorLiveData()

        singleDriverApiResponse()
    }

    // call repo method for init API
    fun callDeliveryMethod(deliveryId : String) {

        singleDeliveryRepo?.callSingleDeliveryApi(deliveryId)
    }

    // add response here for getting
    private fun singleDriverApiResponse() {

        deliveryModel = singleDeliveryRepo?.getResponse()

        changeMediator?.addSource(deliveryModel!!) {

            print("Check in repo")
        }
    }
}