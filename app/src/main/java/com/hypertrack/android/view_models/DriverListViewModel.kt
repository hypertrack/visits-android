package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.DriverListRepo
import com.hypertrack.android.response.DriverList

class DriverListViewModel(application : Application) : AndroidViewModel(application) {

    var driverListRepo: DriverListRepo? = null

    var changeModel: LiveData<ArrayList<DriverList>>? = null

    private var changeMediator: MediatorLiveData<ArrayList<DriverList>>? = null

    init {

        driverListRepo = DriverListRepo(application)

        changeMediator = MediatorLiveData()

        changePasswordApiResponse()
    }

    // call repo method for init API
    fun callDriverListMethod( ) {

        driverListRepo?.callDriverListApi()
    }

    // add response here for getting
    private fun changePasswordApiResponse() {

        changeModel = driverListRepo?.getResponse()

        changeMediator?.addSource(changeModel!!) {

            print("Check in repo")
        }
    }
}