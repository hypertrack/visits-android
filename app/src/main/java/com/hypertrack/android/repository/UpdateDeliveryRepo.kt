package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.utils.MyApplication
import okhttp3.RequestBody

class UpdateDeliveryRepo(applicationContext: Application) {


    var updateResponse: MutableLiveData<Deliveries>? = null

    var updateImageResponse: MutableLiveData<Deliveries>? = null

    var application: MyApplication = applicationContext as MyApplication

    init {

        updateResponse = MutableLiveData()

        updateImageResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callUpdateApi(deliveryId: String, jsonParams: String) {

    }

    // Call driver check in api with proper params
    fun callUuploadImage(deliveryId: String, image: RequestBody) {
    }

    fun getResponse(): MutableLiveData<Deliveries> {

        return updateResponse!!
    }

    fun getImageResponse(): MutableLiveData<Deliveries> {

        return updateImageResponse!!
    }
}