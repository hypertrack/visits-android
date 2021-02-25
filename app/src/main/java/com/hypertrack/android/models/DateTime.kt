package com.hypertrack.android.models

import java.time.ZonedDateTime

interface DateTime {
     fun isBefore(dateTime: DateTime): Boolean
     fun toUnixTimestampSeconds(): Long
}

class DateTimeImpl(val zonedDateTime: ZonedDateTime): DateTime {

    override fun isBefore(dateTime: DateTime): Boolean {
        return toUnixTimestampSeconds() - dateTime.toUnixTimestampSeconds() < 0
    }

    override fun toUnixTimestampSeconds(): Long {
        return zonedDateTime.toEpochSecond()
    }
}