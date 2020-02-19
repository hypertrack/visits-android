package com.hypertrack.android.api_interface

import com.hypertrack.android.response.CheckInResponse
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.response.DriverList
import com.hypertrack.android.response.SingleDriverResponse
import okhttp3.RequestBody import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {


    @GET("logistics/drivers")
    fun getDriverLists() : Call<ArrayList<DriverList>>


    @POST("logistics/drivers/{driver_id}/checkin")
    fun makeDriverCheckIn(@Path("driver_id")driverId : String,@Body json : RequestBody) : Call<CheckInResponse>


    @POST("logistics/drivers/{driver_id}/checkout")
    fun makeDriverCheckOut(@Path("driver_id")driverId : String) : Call<CheckInResponse>


    @GET("logistics/drivers/{driver_id}")
    fun getSingleDriverDetail(@Path("driver_id")driverId : String) : Call<SingleDriverResponse>


    @GET("logistics/deliveries/{delivery_id}")
    fun getSingleDeliveryDetail(@Path("delivery_id")driverId : String) : Call<Deliveries>

    @PATCH("logistics/deliveries/{delivery_id}")
    fun updateDeliveryWithDetails(@Path("delivery_id")driverId : String,@Body json : RequestBody) : Call<Deliveries>


    @POST("logistics/deliveries/{delivery_id}/image")
    fun updateImage(@Path("delivery_id")driverId : String,@Body json : RequestBody) : Call<Deliveries>


    @GET("logistics/deliveries/{delivery_id}/{type}")
    fun updateDeliveryStatusAsCompleted (@Path("delivery_id")driverId : String,
                                         @Path("type")apiType : String) : Call<Deliveries>


}