package com.hypertrack.android.utils

import android.location.Location
import android.os.Build
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatus
import com.hypertrack.android.models.VisitType
import com.hypertrack.sdk.HyperTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) //Location class in Android
@Config(sdk = [Build.VERSION_CODES.P])
class HyperTrackServiceTest {

    @Test
    fun `it should use _geofence_id_ key for geofence visit for id value in completion geotag`() {

        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val visitNote = "valuable customer Note"
        val visit = Visit(
                _id = "42",
                visitNote = visitNote,
                visitType = VisitType.GEOFENCE,
                _state = VisitStatus.COMPLETED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertTrue(payload.isNotEmpty())
        assertTrue(payload.containsKey("geofence_id"))
        assertTrue(payload["geofence_id"] == "42")
    }

    @Test
    fun `it should use _trip_id_ key for trip visit for id value in completion geotag`() {

        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val visitNote = "valuable customer Note"
        val visit = Visit(
                _id = "42",
                visitNote = visitNote,
                visitType = VisitType.TRIP,
                _state = VisitStatus.COMPLETED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertTrue(payload.isNotEmpty())
        assertTrue(payload.containsKey("trip_id"))
        assertTrue(payload["trip_id"] == "42")
    }

    @Test
    fun `it should use _visit_id_ key for local visit for id value in completion geotag`() {

        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val visitNote = "valuable customer Note"
        val visit = Visit(
                _id = "42",
                visitNote = visitNote,
                visitType = VisitType.LOCAL,
                _state = VisitStatus.COMPLETED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertTrue(payload.isNotEmpty())
        assertTrue(payload.containsKey("visit_id"))
        assertTrue(payload["visit_id"] == "42")
    }

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
                _state = VisitStatus.COMPLETED
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
    fun `it should attach visit photo to completion geotag`() {

        val sdk = mockk<HyperTrack>(relaxed = true)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val visitPicture = "abcde"
        val visit = Visit(
                _id = "42",
                visitPicture = visitPicture,
                visitType = VisitType.LOCAL,
                _state = VisitStatus.COMPLETED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertTrue(payload.isNotEmpty())
        assertTrue(payload.containsKey("_visit_photo"))
        assertTrue(payload["_visit_photo"] == visitPicture)
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
                _state = VisitStatus.COMPLETED
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
                _state = VisitStatus.COMPLETED
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
                _state = VisitStatus.COMPLETED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertEquals("42", payload[visit.typeKey])
    }

    @Test
    fun `it should attach expected location to cancel geotag for trips`() {
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
                _state = VisitStatus.CANCELLED
        )

        val slot = slot<Location>()
        every { sdk.addGeotag(any(), capture(slot)) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val expectedLocation = slot.captured
        assertEquals(expectedLat, expectedLocation.latitude, 0.00001)
        assertEquals(expectedLong, expectedLocation.longitude, 0.00001)
    }

    @Test
    fun `it should attach expected location to cancel geotag for geofences`() {
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
                _state = VisitStatus.CANCELLED
        )

        val slot = slot<Location>()
        every { sdk.addGeotag(any(), capture(slot)) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val expectedLocation = slot.captured
        assertEquals(expectedLat, expectedLocation.latitude, 0.00001)
        assertEquals(expectedLong, expectedLocation.longitude, 0.00001)
    }

    @Test
    fun `it should not attach expected location to cancel geotag for local visits`() {
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
                _state = VisitStatus.CANCELLED
        )

        val slot = slot<Map<String, Any>>()
        every { sdk.addGeotag(capture(slot), null) } returns sdk
        hyperTrackService.sendCompletionEvent(visit)

        val payload = slot.captured
        assertEquals("42", payload[visit.typeKey])
    }


}

