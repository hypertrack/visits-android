package com.hypertrack.android.response

import com.hypertrack.android.api.Geofence



class Delivery(private val geofence: Geofence) {
    val id = geofence.geofence_id
    val location = geofence.geometry
}

