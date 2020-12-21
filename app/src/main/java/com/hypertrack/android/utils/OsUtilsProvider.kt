package com.hypertrack.android.utils

import android.content.Context
import android.location.Geocoder
import com.hypertrack.android.models.Address
import com.hypertrack.logistics.android.github.R
import java.text.SimpleDateFormat
import java.util.*

class OsUtilsProvider(private val context: Context, private val crashReportsProvider: CrashReportsProvider) {
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
                    address.thoroughfare ?: context.getString(R.string.unnamed_street),
                    address.postalCode ?: "",
                    address.locality ?: context.getString(R.string.unknown_city),
                    address.countryName ?: context.getString(R.string.unknown_country)
                )
            }
        } catch (t: Throwable) {
            crashReportsProvider.logException(t)
        }
        return Address(
            context.getString(R.string.unknown_location_at)+ "($latitude, $longitude)",
            "",
            "",
            ""
        )
    }

    fun getStringResourceForId(resId: Int): String = context.getString(resId)
}