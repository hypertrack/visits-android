package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.CheckInRepo
import com.hypertrack.android.response.CheckInResponse

class CheckInViewModel(application : Application) : AndroidViewModel(application) {

    var checkInRepo: CheckInRepo? = null

    var changeModel: LiveData<CheckInResponse>? = null

    private var changeMediator: MediatorLiveData<CheckInResponse>? = null

    init {

        checkInRepo = CheckInRepo(application)

        changeMediator = MediatorLiveData()

        changePasswordApiResponse()
    }

    // call repo method for init API
    fun callCheckInMethod(driverId : String,body: String) {

        checkInRepo?.callCheckInApi(driverId,body)
    }

    // add response here for getting
    private fun changePasswordApiResponse() {

        changeModel = checkInRepo?.getResponse()

        changeMediator?.addSource(changeModel!!) {

            print("Check in repo")
        }
    }
}