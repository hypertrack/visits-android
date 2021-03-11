package com.hypertrack.android.utils

import org.junit.Test

import org.junit.Assert.*

class SimpleTimeDistanceFormatterTest {

    @Test
    fun formatTime() {
        val formatter = SimpleTimeDistanceFormatter()
        val timestamp = "2020-02-02T20:02:02.000Z"
        val got = formatter.formatTime(timestamp)
        assertEquals("8:02pm", got)
    }
}