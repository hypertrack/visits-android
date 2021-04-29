package com.hypertrack.android.models

import com.hypertrack.android.api.Point
import com.hypertrack.android.api.TripDestination
import com.hypertrack.android.createBaseOrder
import com.hypertrack.android.models.local.LocalOrder
import com.hypertrack.android.ui.common.formatDateTime
import junit.framework.Assert.assertEquals
import org.junit.Test

class LocalOrderTest {

    @Test
    fun `it should fallback-if-null trip address to scheduled_at and then to coordinates`() {
        val scheduledAt = "2012-02-02T20:20:02.020Z"
        LocalOrder(
            order = createBaseOrder().copy(scheduledAt = scheduledAt),
            true,
            null
        ).apply {
            assertEquals(scheduledAt.formatDateTime(), shortAddress)
        }

        LocalOrder(
            order = createBaseOrder().copy(
                scheduledAt = null, destination = TripDestination(
                    null,
                    Point(listOf(42.0, 42.0)),
                    arrivedAt = "2020-02-02T20:20:02.020Z"
                )
            ),
            true,
            null
        ).apply {
            assertEquals("42.0, 42.0", shortAddress)
        }
    }

}