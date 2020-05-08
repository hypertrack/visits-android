package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.*
import com.hypertrack.android.repository.CheckInRepo
import com.hypertrack.android.repository.DriverRepository

class DriverLoginViewModel(private val driverRepository : DriverRepository) : ViewModel() {

    /**
     * Request a snackbar to display a string.
     *
     * This variable is private because we don't want to expose MutableLiveData
     *
     * MutableLiveData allows anyone to set a value, and MainViewModel is the only
     * class that should be setting values.
     */
    private val _snackBar = MutableLiveData<String?>()


    var checkInRepo: CheckInRepo? = null

    var changeModel: LiveData<Boolean>? = null

    private var changeMediator: MediatorLiveData<Unit>? = null

    init {

//        checkInRepo = CheckInRepo(application)

        changeMediator = MediatorLiveData()

    }

    // call repo method for init API
    fun callCheckInMethod(driverId: String) {
        checkInRepo?.callCheckInApi(driverId)
    }
}