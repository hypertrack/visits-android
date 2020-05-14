package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.Driver
import com.hypertrack.android.repository.UpdateDeliveryRepo

class UpdateDeliveryViewModel(application : Application) : AndroidViewModel(application) {

    var updateRepo: UpdateDeliveryRepo? = null

    var updateModel: LiveData<Delivery>? = null

    private var changeMediator: MediatorLiveData<Driver>? = null

    init {

        updateRepo = UpdateDeliveryRepo(application)

        changeMediator = MediatorLiveData()

        changePasswordApiResponse()
    }

    // call repo method for init API
    fun callUpdateDelivery(deliveryId : String,body: String) {

        updateRepo?.callUpdateApi(deliveryId,body)
    }

    // add response here for getting
    private fun changePasswordApiResponse() {

        updateModel = updateRepo?.getResponse()

        changeMediator?.addSource(updateModel!!) {

            print("Check in repo")
        }
    }
}