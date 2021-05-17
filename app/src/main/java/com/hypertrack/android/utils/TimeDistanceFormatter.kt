package com.hypertrack.android.utils

import com.hypertrack.logistics.android.github.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.math.round

interface TimeDistanceFormatter {
    /** 2020-02-02T20:02:02.000Z -> 20:02 or 8:02pm adjusted to device's timezone */
    fun formatTime(isoTimestamp: String): String

    /** 2400 meters -> 2.4 km or 1.5 mi */
    fun formatDistance(meters: Int): String
}

open class SimpleTimeDistanceFormatter(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : TimeDistanceFormatter {

    private val format = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    protected val shouldUseImperial = Locale.getDefault().country in listOf("US", "LR", "MM")

    override fun formatTime(isoTimestamp: String): String {
        return try {
            Instant.parse(isoTimestamp).atZone(zoneId).toLocalTime().format(format)
                .replace(" PM", "pm").replace(" AM", "am")
        } catch (ignored: Exception) {
            isoTimestamp
        }
    }

    override fun formatDistance(meters: Int): String {
        if (shouldUseImperial) {
            val miles = meters / 1609.0
            return "${"%.1f".format(miles)} mi"
        }
        val kms = meters / 1000.0
        return "${"%.1f".format(kms)} km"
    }

    companion object {
        const val TAG = "TimeDistanceFormatter"
    }
}

class LocalizedTimeDistanceFormatter(
    private val osUtilsProvider: OsUtilsProvider,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : SimpleTimeDistanceFormatter(zoneId) {

    override fun formatDistance(meters: Int): String {
        val format = when {
            meters == 0 -> "%.0f"
            meters < 0.01 * 1609.0 -> "%.3f"
            else -> "%.1f"
        }
        val res = if (shouldUseImperial) {
            val miles = meters / 1609.0
            osUtilsProvider.stringFromResource(R.string.miles, format.format(miles))
        } else {
            val kms = meters / 1000.0
            osUtilsProvider.stringFromResource(R.string.kms, format.format(kms))
        }
        return res
    }

    companion object {
        const val TAG = "TimeDistanceFormatter"
    }
}