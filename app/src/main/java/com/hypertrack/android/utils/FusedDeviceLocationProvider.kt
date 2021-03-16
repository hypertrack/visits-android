package com.hypertrack.android.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.ui.screens.visits_management.tabs.history.DeviceLocationProvider

class FusedDeviceLocationProvider(private val context: Context) : DeviceLocationProvider {
    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(block: (l:LatLng?) -> Unit) {
        LocationServices.getFusedLocationProviderClient(context).lastLocation
            .addOnCompleteListener {
                if (it.isSuccessful)
                    block(LatLng(it.result.latitude, it.result.longitude))
                else
                    block(null)
            }
            .addOnFailureListener {
                block(null)
            }

    }
}