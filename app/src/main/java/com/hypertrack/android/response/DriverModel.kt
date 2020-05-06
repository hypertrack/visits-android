package com.hypertrack.android.response

import com.hypertrack.android.api.Geofence


data class DriverModel(
    val device_id: String,
    val driver_id: String,
    val deliveries: List<Delivery>
)

class Delivery(private val geofence: Geofence) {
    val id = geofence.geofence_id
    val location = geofence.geometry
}

