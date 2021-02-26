package com.hypertrack.android.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.hypertrack.android.models.Address
import com.hypertrack.logistics.android.github.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
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

    fun getAddressFromCoordinates(latitude: Double, longitude: Double): Address {
        try {
            val coder = Geocoder(context)
            val address = coder.getFromLocation(latitude, longitude, 1)?.get(0)
            address?.let {
                return Address(
                        street = address.thoroughfare ?: stubStreet(latitude, longitude),
                        postalCode = address.postalCode,
                        city = address.locality,
                        country = address.countryName
                )
            }
        } catch (t: Throwable) {
            when (t) {
                is java.io.IOException -> Log.w(TAG, "Can't get the address", t)
                else -> crashReportsProvider.logException(t)
            }
        }
        return Address(
                street = stubStreet(latitude, longitude),
                postalCode = null,
                city = null,
                country = null
        )
    }

    fun getLocalDate(): LocalDate = LocalDate.now()

    fun getTimeZoneId(): ZoneId = ZoneId.systemDefault()

    private fun stubStreet(latitude: Double, longitude: Double) =
            context.getString(R.string.unknown_location_at) + "($latitude, $longitude)"

    fun getString(resId: Int): String = context.getString(resId)

    companion object {
        const val TAG = "OsUtilsProvider"
    }
}