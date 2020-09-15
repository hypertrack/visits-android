package com.hypertrack.android.utils

import com.hypertrack.android.models.Visit
import com.hypertrack.sdk.HyperTrack
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class HyperTrackServiceTest {

    @Test
    fun `it should attach visit note to completion geotag`() {

        val sdk = mock(HyperTrack::class.java)
        val listener = TrackingState()

        val hyperTrackService = HyperTrackService(listener, sdk)
        val visitNote = "valuable customer Note"
        val visit = Visit(_id = "42", visitNote = visitNote)

        hyperTrackService.sendCompletionEvent(visit._id, visit.visitNote)

        val captor = argumentCaptor<Map<String, Any>>()

        verify(sdk).addTripMarker(captor.capture())
        val payload = captor.value
        assertTrue(payload.isNotEmpty())
        assertTrue(payload.containsKey("delivery_note"))
        assertTrue(payload["delivery_note"] == visitNote)
    }

}
inline fun <reified T : Any> argumentCaptor() = ArgumentCaptor.forClass(T::class.java)
