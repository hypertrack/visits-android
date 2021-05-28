package com.hypertrack.android.ui.common

import android.location.Address
import com.google.android.libraries.places.api.model.Place
import com.hypertrack.android.models.Location

object LocationUtils {
    fun distanceMeters(location: Location?, location1: Location?): Int? {
        try {
            if (location != null && location1 != null
                && !(location.latitude == 0.0 && location.longitude == 0.0)
                && !(location1.latitude == 0.0 && location1.longitude == 0.0)
            ) {
                val res = FloatArray(3)
                android.location.Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    location1.latitude,
                    location1.longitude,
                    res
                );
                return res[0].toInt()
            } else {
                return null
            }
        } catch (_: Exception) {
            return null
        }
    }
}

fun Address.toAddressString(): String {
    val localityString = (locality?.let { "$it, " } ?: "")
    val address = if (thoroughfare == null) {
        " ${latitude}, ${longitude}"
    } else {
        " $thoroughfare${subThoroughfare?.let { ", $it" } ?: ""}"
    }
    return "$localityString$address"
}

fun Address.toShortAddressString(): String {
    val address = if (thoroughfare == null) {
        "${latitude}, ${longitude}"
    } else {
        "$thoroughfare${subThoroughfare?.let { ", $it" } ?: ""}"
    }
    return address
}

fun Place.toAddressString(): String {
    val locality =
        addressComponents?.asList()?.filter { "locality" in it.types }?.firstOrNull()?.name
            ?: addressComponents?.asList()?.filter { "administrative_area_level_1" in it.types }
                ?.firstOrNull()?.name
            ?: addressComponents?.asList()?.filter { "administrative_area_level_2" in it.types }
                ?.firstOrNull()?.name
            ?: addressComponents?.asList()?.filter { "political" in it.types }?.firstOrNull()?.name
    val thoroughfare =
        addressComponents?.asList()?.filter { "route" in it.types }?.firstOrNull()?.name
    val subThoroughfare =
        addressComponents?.asList()?.filter { "street_number" in it.types }?.firstOrNull()?.name

    val localityString = (locality?.let { "$it, " } ?: "")
    val address = if (thoroughfare == null) {
        " ${latLng?.latitude}, ${latLng?.longitude}"
    } else {
        " $thoroughfare${subThoroughfare?.let { ", $it" } ?: ""}"
    }
    return "$localityString$address"
}

