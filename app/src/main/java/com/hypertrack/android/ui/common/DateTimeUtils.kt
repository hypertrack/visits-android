package com.hypertrack.android.ui.common

import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateTimeUtils {
    fun secondsToLocalizedString(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        if (hours > 0) {
            return MyApplication.context.getString(R.string.duration, hours, minutes)
        } else {
            return MyApplication.context.getString(R.string.duration_minutes, minutes)
        }
    }
}

fun String.formatDateTime(): String {
    return ZonedDateTime.parse(this)
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
}