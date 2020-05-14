package com.hypertrack.android.response

import com.hypertrack.android.repository.Delivery

data class DriverDeliveries(
    val _id: String,
    val driver_id: String,
    val name: String,
    val device_id: String,
    val token: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: String,
    val active_trip: String,
    val app_name: String,
    val platform: String,
   val deliveries : ArrayList<Delivery>
)

// showNavigationItem =  this object only for sho navigation icon in pending category deliveries

