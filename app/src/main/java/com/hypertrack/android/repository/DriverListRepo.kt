package com.hypertrack.android.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.response.CheckInResponse
import com.hypertrack.android.response.DriverList
import com.hypertrack.android.utils.MyApplication
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Driver

class DriverListRepo(applicationContext : Application) {


    var driverListResponse: MutableLiveData<ArrayList<DriverList>>? = null

    var application : MyApplication = applicationContext as MyApplication

    init {

        driverListResponse = MutableLiveData()

    }

    // Call driver check in api with proper params
    fun callDriverListApi() {
//
//        val changePasswordCall = application .getApiClient().getDriverLists()
//
//        changePasswordCall.enqueue(object : Callback<ArrayList<DriverList>> {
//
//            override fun onFailure(call: Call<ArrayList<DriverList>>, t: Throwable) {
//
//                driverListResponse?.postValue(null)
//            }
//
//            override fun onResponse(
//                call: Call<ArrayList<DriverList>>,
//                response: Response<ArrayList<DriverList>>
//            ) {
//
//                driverListResponse?.postValue(response.body())
//            }
//        })
    }

    fun getResponse(): MutableLiveData<ArrayList<DriverList>> {

        return driverListResponse!!
    }
}