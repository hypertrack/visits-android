package com.hypertrack.android.ui.common

import android.location.Address

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