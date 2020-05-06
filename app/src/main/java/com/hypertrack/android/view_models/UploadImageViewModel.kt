package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hypertrack.android.repository.UpdateDeliveryRepo
import com.hypertrack.android.response.DriverModel
import com.hypertrack.android.response.Deliveries
import okhttp3.RequestBody

class UploadImageViewModel(application : Application) : AndroidViewModel(application) {

    var updateRepo: UpdateDeliveryRepo? = null


    var updateImageModel: LiveData<Deliveries>? = null

    private var changeMediator: MediatorLiveData<DriverModel>? = null

    init {

        updateRepo = UpdateDeliveryRepo(application)

        changeMediator = MediatorLiveData()

        changePasswordApiResponse()
    }

    // call repo method for init API
    fun callUpdateImage(deliveryId : String,body: RequestBody) {

        updateRepo?.callUuploadImage(deliveryId,body)
    }

    // add response here for getting
    private fun changePasswordApiResponse() {


        updateImageModel = updateRepo?.getImageResponse()


        changeMediator?.addSource(updateImageModel!!) {

            print("Check in repo")
        }
    }
}