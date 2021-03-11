package com.hypertrack.android.utils

import org.junit.Test

import org.junit.Assert.*
import java.time.ZoneId

class SimpleTimeDistanceFormatterTest {

    @Test
    fun formatTime() {
        val timestamp = "2020-02-02T20:02:02.000Z"
        val zpFormatter = SimpleTimeDistanceFormatter(ZoneId.of("Europe/Zaporozhye"))
        assertEquals("10:02pm", zpFormatter.formatTime(timestamp))
        val usFormatter = SimpleTimeDistanceFormatter(ZoneId.of("America/Los_Angeles"))
        assertEquals("12:02pm", usFormatter.formatTime(timestamp))
        val inFormatter  = SimpleTimeDistanceFormatter(ZoneId.of("Asia/Calcutta"))
        assertEquals("1:32am", inFormatter.formatTime(timestamp))
    }
}