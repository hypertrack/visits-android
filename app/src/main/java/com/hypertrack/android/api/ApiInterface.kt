package com.hypertrack.android.api

import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.response.DriverDeliveries
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {


    @POST("client/devices/{device_id}/start")
    fun makeDriverCheckIn(@Path("device_id") deviceId: String) : Call<Unit>

    @POST("client/devices/{device_id}/stop")
    fun makeDriverCheckOut(@Path("device_id")deviceId : String) : Call<Unit>

    @GET("client/devices/{device_id}/geofences")
    fun getDeliveries(@Path("device_id")deviceId : String) : Call<DriverDeliveries>

    @GET("client/devices/{device_id}/geofences/{delivery_id}")
    fun getSingleDeliveryDetail(@Path("delivery_id")deliveryId : String) : Call<Deliveries>

}