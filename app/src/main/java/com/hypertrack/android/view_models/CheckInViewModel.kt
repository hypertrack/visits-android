package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.CheckInRepo

class CheckInViewModel(application : Application) : AndroidViewModel(application) {

    var checkInRepo: CheckInRepo? = null

    var changeModel: LiveData<Unit>? = null

    private var changeMediator: MediatorLiveData<Unit>? = null

    init {

        checkInRepo = CheckInRepo(application)

        changeMediator = MediatorLiveData()

    }

    // call repo method for init API
    fun callCheckInMethod(driverId: String) {
        checkInRepo?.callCheckInApi(driverId)
    }
}