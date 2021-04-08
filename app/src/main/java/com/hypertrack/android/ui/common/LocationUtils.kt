package com.hypertrack.android.ui.common

import android.location.Address
import com.google.android.libraries.places.api.model.Place

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