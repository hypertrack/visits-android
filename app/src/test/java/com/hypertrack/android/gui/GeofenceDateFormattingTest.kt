package com.hypertrack.android.gui

import com.hypertrack.android.ui.common.formatDate
import com.hypertrack.android.ui.common.formatDateTime
import com.hypertrack.android.ui.screens.place_details.PlaceVisitsAdapter
import com.hypertrack.android.utils.SimpleTimeDistanceFormatter
import com.hypertrack.android.utils.TimeDistanceFormatter
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
        val formatter = object : TimeDistanceFormatter {
            override fun formatTime(isoTimestamp: String): String {
                return isoTimestamp
            }

            override fun formatDistance(meters: Int): String {
                return meters.toString()
            }
        }
        val adapter = PlaceVisitsAdapter(mockk() {
            every { stringFromResource(R.string.place_today) } returns "Today"
            every { stringFromResource(R.string.place_yesterday) } returns "Yesterday"
        }, formatter)
        val today = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"))
            .withHour(13).withMinute(1)
        val yesterday = today.minusDays(1)
        val weekAgo = today.minusDays(7)
        val longTimeAgo = today.minusDays(14)

        test(adapter, today, today) { res, dt1, dt2 ->
            assertEquals("Today, ${formatter.formatTime(dt1)} — ${formatter.formatTime(dt2)}", res)
        }

        test(adapter, yesterday, yesterday) { res, dt1, dt2 ->
            assertEquals(
                "Yesterday, ${formatter.formatTime(dt1)} — ${formatter.formatTime(dt2)}",
                res
            )
        }

        test(adapter, yesterday, today) { res, dt1, dt2 ->
            assertEquals(
                "Yesterday, ${formatter.formatTime(dt1)} — Today, ${
                    formatter.formatTime(
                        dt2
                    )
                }", res
            )
        }

        test(adapter, weekAgo, yesterday) { res, dt1, dt2 ->
            assertEquals(
                "${weekAgo.formatDate()}, ${formatter.formatTime(dt1)} — Yesterday, ${
                    formatter.formatTime(
                        dt2
                    )
                }", res
            )
        }

        test(adapter, weekAgo, weekAgo) { res, dt1, dt2 ->
            assertEquals(
                "${weekAgo.formatDate()}, ${formatter.formatTime(dt1)} — ${
                    formatter.formatTime(
                        dt2
                    )
                }", res
            )
        }

        test(adapter, longTimeAgo, weekAgo) { res, dt1, dt2 ->
            assertEquals(
                "${longTimeAgo.formatDate()}, ${formatter.formatTime(dt1)} — ${weekAgo.formatDate()}, ${
                    formatter.formatTime(
                        dt2
                    )
                }", res
            )
        }

    }

    fun test(
        adapter: PlaceVisitsAdapter,
        baseDt1: ZonedDateTime,
        baseDt2: ZonedDateTime,
        checks: (res: String, dt1: String, dt2: String) -> Unit
    ) {
        val dt1 = baseDt1.format(DateTimeFormatter.ISO_INSTANT)
        val dt2 = baseDt2.withMinute(30).withSecond(1).format(DateTimeFormatter.ISO_INSTANT)

        adapter.formatDate(dt1, dt2).let {
            checks.invoke(
                it,
                dt1.format(DateTimeFormatter.ISO_INSTANT),
                dt2.format(DateTimeFormatter.ISO_INSTANT)
            )
        }
    }

}