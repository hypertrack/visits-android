package com.hypertrack.android.utils

import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

interface TimeDistanceFormatter {
    /** 2020-02-02T20:02:02.000Z -> 20:02 or 8:02pm adjusted to device's timezone */
    fun formatTime(isoTimestamp: String) : String
    /** 2400 meters -> 2.4 km or 1.5 mi */
    fun formatDistance(meters: Int) : String
}

class SimpleTimeDistanceFormatter : TimeDistanceFormatter {

    private val format = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private val isRoyalSystem = Locale.getDefault().country in listOf("US", "GB")

    override fun formatTime(isoTimestamp: String): String {
        return try {
            val time = ZonedDateTime.parse(isoTimestamp).toLocalTime()
            Log.d(TAG, "Created time $time")
            val result = time.format(format)
            Log.d(TAG, "Created timestamp $result")
            result.replace(" PM", "pm").replace(" AM", "am")
        } catch (ignored: Exception) {
            ""
        }
    }

    override fun formatDistance(meters: Int): String {
        if (isRoyalSystem) {
            val miles = meters / 1609.0
            return "${"%.1f".format(miles)} mi"
        }
        val kms = meters / 1000.0
        return "${"%.1f".format(kms)} km"
    }

    companion object { const val TAG = "TimeDistanceFormatter" }
}