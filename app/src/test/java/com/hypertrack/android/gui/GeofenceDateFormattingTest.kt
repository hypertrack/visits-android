package com.hypertrack.android.gui

import com.hypertrack.android.ui.common.formatDate
import com.hypertrack.android.ui.common.formatDateTime
import com.hypertrack.android.ui.screens.place_details.PlaceVisitsAdapter
import com.hypertrack.logistics.android.github.R
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GeofenceDateFormattingTest {

    @Test
    fun `it should properly format enter and exit range`() {
        val adapter = PlaceVisitsAdapter(mockk() {
            every { stringFromResource(R.string.place_today) } returns "Today"
            every { stringFromResource(R.string.place_yesterday) } returns "Yesterday"
        })
        val today = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"))
            .withHour(13).withMinute(1)
        val yesterday = today.minusDays(1)
        val weekAgo = today.minusDays(7)
        val longTimeAgo = today.minusDays(14)

        adapter.formatDate(
            today.format(DateTimeFormatter.ISO_INSTANT),
            today.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)
        ).let {
            assertEquals("Today, 13:01 — 13:30", it)
        }

        adapter.formatDate(
            yesterday.format(DateTimeFormatter.ISO_INSTANT),
            yesterday.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)
        ).let {
            assertEquals("Yesterday, 13:01 — 13:30", it)
        }

        adapter.formatDate(
            today.format(DateTimeFormatter.ISO_INSTANT),
            yesterday.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)
        ).let {
            assertEquals("Today, 13:01 — Yesterday, 13:30", it)
        }

        adapter.formatDate(
            weekAgo.format(DateTimeFormatter.ISO_INSTANT),
            yesterday.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)
        ).let {
            assertEquals("${weekAgo.formatDate()}, 13:01 — Yesterday, 13:30", it)
        }

        adapter.formatDate(
            weekAgo.format(DateTimeFormatter.ISO_INSTANT),
            weekAgo.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)
        ).let {
            assertEquals("${weekAgo.formatDate()}, 13:01 — 13:30", it)
        }

        adapter.formatDate(
            weekAgo.format(DateTimeFormatter.ISO_INSTANT),
            longTimeAgo.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)
        ).let {
            assertEquals("${weekAgo.formatDate()}, 13:01 — ${longTimeAgo.formatDate()}, 13:30", it)
        }
    }

}