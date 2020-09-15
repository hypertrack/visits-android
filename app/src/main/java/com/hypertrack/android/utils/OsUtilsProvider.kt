package com.hypertrack.android.utils

import android.content.Context
import android.location.Geocoder
import com.hypertrack.android.models.Address
import java.text.SimpleDateFormat
import java.util.*

class OsUtilsProvider(private val context: Context) {
    fun getCurrentTimestamp(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(Date())
    }

    fun getFineDateTimeString(): String {
        val df = SimpleDateFormat("MMM d, h:mm", Locale.ENGLISH)
        df.timeZone = TimeZone.getDefault()

        val postfixFmt = SimpleDateFormat("a", Locale.ENGLISH)
        postfixFmt.timeZone = TimeZone.getDefault()

        val now = Date()
        return "${df.format(now)}${postfixFmt.format(now).toLowerCase(Locale.ENGLISH)}"
    }

    fun getAddressFromCoordinates(latitude: Double, longitude: Double) : Address {
        try {
            val coder = Geocoder(context)
            val address = coder.getFromLocation(latitude, longitude, 1)?.get(0)
            address?.let {
                return Address(
                    address.thoroughfare ?: "Unnamed street",
                    address.postalCode ?: "",
                    address.locality ?: "Unknown city",
                    address.countryName ?: "Nowhere"
                )
            }
        } catch (_: Throwable) { }
        return Address(
            "Unknown location at ($latitude, $longitude)",
            "",
            "",
            ""
        )
    }
}