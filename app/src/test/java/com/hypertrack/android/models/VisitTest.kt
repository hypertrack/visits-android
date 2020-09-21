package com.hypertrack.android.models

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class VisitTest {
    @Test
    fun `it should not delete local Visits`() {
        val localVisit = Visit("42", "42", visitType = VisitType.LOCAL)
        assertFalse(localVisit.isDeletable)
    }

    @Test
    fun `it should not delete trips completed during the last day`() {
        val completedAt = Instant.now().minusSeconds(3600).toString()
        println("Completed at $completedAt")
        val recentTrip = Visit("42", "42", completedAt = completedAt, visitType = VisitType.TRIP)
        assertFalse(recentTrip.isDeletable)
    }

    @Test
    fun `it should delete trips completed earlier than the the last day`() {
        val completedAt = Instant.now().minus(2, ChronoUnit.DAYS).toString()
        println("Completed at $completedAt")

        val recentTrip = Visit("42", "42", completedAt = completedAt, visitType = VisitType.TRIP)
        assertTrue(recentTrip.isDeletable)
    }
}