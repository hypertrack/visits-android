package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.CheckInRepo
import com.hypertrack.android.repository.CheckOutRepo
import com.hypertrack.android.response.CheckInResponse

class CheckOutViewModel(application : Application) : AndroidViewModel(application) {

    var checkOutRepo: CheckOutRepo? = null

    var changeModel: LiveData<CheckInResponse>? = null

    private var changeMediator: MediatorLiveData<CheckInResponse>? = null

    init {

        checkOutRepo = CheckOutRepo(application)

        changeMediator = MediatorLiveData()

        CheckOutApiResponse()
    }

    // call repo method for init API
    fun callCheckOutMethod(driverId : String) {

        checkOutRepo?.callCheckOutApi(driverId)
    }

    // add response here for getting
    private fun CheckOutApiResponse() {

        changeModel = checkOutRepo?.getResponse()

        changeMediator?.addSource(changeModel!!) {

            print("Check in repo")
        }
    }
}