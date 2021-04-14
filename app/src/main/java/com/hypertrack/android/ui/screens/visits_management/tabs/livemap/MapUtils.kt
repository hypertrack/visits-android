package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.maps.google.widget.GoogleMapConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable

object MapUtils {
    @JvmStatic
    fun getBuilder(context: Context): GoogleMapConfig.Builder {
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        return GoogleMapConfig.newBuilder(context)
            .boundingBoxDimensions(width, (height / 1.9).toInt())
    }

    @JvmStatic
    fun getAddress(context: Context?, latLng: LatLng): Callable<String> {
        val geocoder = Geocoder(context, Locale.getDefault())
        return label@ Callable {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isEmpty()) {
                    var formattedAddress = ""
                    val address = addresses[0]
                    if (address.subThoroughfare != null) {
                        formattedAddress += address.subThoroughfare
                    }
                    if (address.thoroughfare != null) {
                        if (!formattedAddress.isEmpty()) {
                            formattedAddress += " "
                        }
                        formattedAddress += address.thoroughfare
                    }
                    if (address.locality != null) {
                        if (!formattedAddress.isEmpty()) {
                            formattedAddress += ", "
                        }
                        formattedAddress += address.locality
                    }
                    formattedAddress
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            ""
        }
    }

    suspend fun getLocationAddress(context: Context, latLng: LatLng): String = suspendCancellableCoroutine {
        try {
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                var formattedAddress = ""
                val address = addresses[0]
                if (address.subThoroughfare != null) {
                    formattedAddress += address.subThoroughfare
                }
                if (address.thoroughfare != null) {
                    if (!formattedAddress.isEmpty()) {
                        formattedAddress += " "
                    }
                    formattedAddress += address.thoroughfare
                }
                if (address.locality != null) {
                    if (!formattedAddress.isEmpty()) {
                        formattedAddress += ", "
                    }
                    formattedAddress += address.locality
                }
                it.resume(formattedAddress) {}
            }
        } catch (e: IOException) {
            e.printStackTrace()
            it.resume("") {}
        }
    }
}