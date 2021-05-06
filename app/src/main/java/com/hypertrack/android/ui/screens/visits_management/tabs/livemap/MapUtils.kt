package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.maps.google.widget.GoogleMapConfig
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object MapUtils {
    @JvmStatic
    fun getBuilder(context: Context): GoogleMapConfig.Builder {
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        return GoogleMapConfig.newBuilder(context)
            .boundingBoxDimensions(width, (height / 1.9).toInt())
    }

    suspend fun getLocationAddress(context: Context, latLng: LatLng): String = suspendCoroutine {
        try {
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                var formattedAddress = listOfNotNull(address.subThoroughfare, address.thoroughfare).joinToString(" ")
                formattedAddress += address.locality?.let { locality ->
                        if (formattedAddress.isBlank()) locality else ", $locality"
                }
                it.resume(formattedAddress)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            it.resume("")
        }
    }
}