package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.CheckInRepo
import com.hypertrack.android.repository.SingleDriverRepo
import com.hypertrack.android.response.CheckInResponse
import com.hypertrack.android.response.SingleDriverResponse

class SingleDriverViewModel(application : Application) : AndroidViewModel(application) {

    var singleDriverRepo: SingleDriverRepo? = null

    var driverModel: LiveData<SingleDriverResponse>? = null

    private var changeMediator: MediatorLiveData<SingleDriverResponse>? = null

    init {

        singleDriverRepo = SingleDriverRepo(application)

        changeMediator = MediatorLiveData()

        singleDriverApiResponse()
    }

    // call repo method for init API
    fun callFetchDeliveries(driverId : String) {

        singleDriverRepo?.callSingleDriverApi(driverId)
    }

    // add response here for getting
    private fun singleDriverApiResponse() {

        driverModel = singleDriverRepo?.getResponse()

        changeMediator?.addSource(driverModel!!) {

            print("Check in repo")
        }
    }
}