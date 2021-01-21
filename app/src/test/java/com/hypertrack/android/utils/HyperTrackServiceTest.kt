package com.hypertrack.android.utils

import android.location.Location
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatus
import com.hypertrack.android.models.VisitType
import com.hypertrack.sdk.HyperTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class) //Location class in Android
class HyperTrackServiceTest {

    @Test
    fun `it should attach visit note to completion geotag`() {

        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val visitNote = "valuable customer Note"
        val visit = Visit(
            _id = "42",
            visitNote = visitNote,
            visitType = VisitType.LOCAL,
            _state = VisitStatus.VISITED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertTrue(payload.isNotEmpty())
        assertTrue(payload.containsKey("visit_note"))
        assertTrue(payload["visit_note"] == visitNote)
    }

    @Test
    fun `it should attach expected location to check out geotag for trips`() {
        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val expectedLat = 42.0
        val expectedLong = 3.14
        val visit = Visit(
            _id = "42",
            latitude = expectedLat,
            longitude = expectedLong,
            visitType = VisitType.TRIP,
            _state = VisitStatus.VISITED
        )

        val slot = slot<Location>()
        every { sdk.addGeotag(any(), capture(slot)) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val expectedLocation = slot.captured
        assertEquals(expectedLat, expectedLocation.latitude, 0.00001)
        assertEquals(expectedLong, expectedLocation.longitude, 0.00001)
    }

    @Test
    fun `it should attach expected location to check out geotag for geofences`() {
        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val expectedLat = 2.1828
        val expectedLong = 3.1415
        val visit = Visit(
            _id = "42",
            latitude = expectedLat,
            longitude = expectedLong,
            visitType = VisitType.GEOFENCE,
            _state = VisitStatus.VISITED
        )

        val slot = slot<Location>()
        every { sdk.addGeotag(any(), capture(slot)) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val expectedLocation = slot.captured
        assertEquals(expectedLat, expectedLocation.latitude, 0.00001)
        assertEquals(expectedLong, expectedLocation.longitude, 0.00001)
    }

    @Test
    fun `it should not attach expected location to check out geotag for local visits`() {
        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val expectedLat = 2.1828
        val expectedLong = 3.1415
        val visit = Visit(
            _id = "42",
            latitude = expectedLat,
            longitude = expectedLong,
            visitType = VisitType.LOCAL,
            _state = VisitStatus.VISITED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertEquals("42", payload[visit.typeKey])
    }

}

