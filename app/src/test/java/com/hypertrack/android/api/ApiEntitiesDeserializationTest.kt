package com.hypertrack.android.api

import com.hypertrack.android.utils.Injector
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test

class ApiEntitiesDeserializationTest {

    private val moshi = Injector.getMoshi()


    @Test
    fun `it should deserialize geofence api responses from Json`() {
        val geofenceResponseString =
            """
            {
                "data": [
                    {
                        "geofence_id": "010b7861-59fc-4157-9fcd-6d2e0c5072d9",
                        "account_id": "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                        "device_id": "86BB603D-B905-367D-AE3B-3ECFA4428D96",
                        "single_use": false,
                        "created_at": "2020-01-16T12:51:00.010934+00:00",
                        "metadata": { "location": "Ferry Building" },
                        "geometry": {
                            "type": "Point",
                            "coordinates": [ -122.394, 37.7957 ]
                        },
                        "markers": { 
                           "data": [{
                                "geofence_id": "42-42",
                                "arrival": { "recorded_at": "2020-02-02T20:02:02.000Z" }
                           }],
                           "pagination_token": null
                        },
                        "archived": false,
                        "geofence_type": "device",
                        "radius": 30
                    }
                ],
                "pagination_token": null,
                "links": { "next": null }
            }
            """

        val geofenceResponse = moshi.adapter(GeofenceResponse::class.java).fromJson(geofenceResponseString)
        assertNotNull(geofenceResponse)
        assertNotNull(geofenceResponse?.geofences)
        val geofences = geofenceResponse?.geofences?: throw NullPointerException("no geofences in response")
        val geofence = geofences.first()
        assertEquals("010b7861-59fc-4157-9fcd-6d2e0c5072d9", geofence.geofence_id)
        assertEquals("2020-01-16T12:51:00.010934+00:00", geofence.created_at)
        assertEquals(30, geofence.radius)
    }

    @Test
    fun `it should deserialize geofences correctly`() {
        val visitedGeofence = """
                            {
                                "geofence_id": "010b7861-59fc-4157-9fcd-6d2e0c5072d9",
                                "account_id": "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                                "device_id": "86BB603D-B905-367D-AE3B-3ECFA4428D96",
                                "single_use": false,
                                "created_at": "2020-01-16T12:51:00.010934+00:00",
                                "metadata": { "location": "Ferry Building" },
                                "geometry": {
                                    "type": "Point",
                                    "coordinates": [ -122.394, 37.7957 ]
                                },
                                "markers": { 
                                   "data": [{
                                        "geofence_id": "42-42",
                                        "arrival": { "recorded_at": "2020-02-02T20:02:02.000Z" }
                                   }],
                                   "pagination_token": null
                                },
                                "archived": false,
                                "geofence_type": "device",
                                "radius": 30
                            }
                            """.trimIndent()
        val geofence = moshi.adapter(Geofence::class.java)
            .fromJson(visitedGeofence)
            ?: throw NullPointerException("No geofence deserialized")
        assertEquals("010b7861-59fc-4157-9fcd-6d2e0c5072d9", geofence.geofence_id)
        assertEquals("2020-01-16T12:51:00.010934+00:00", geofence.created_at)
        assertEquals(30, geofence.radius)
        assertEquals("2020-02-02T20:02:02.000Z", geofence.visitedAt)

    }

    @Test
    fun `it should deserialize markers correctly`() {
        val serializedMarker = """ 
            { "geofence_id": "42-42", "arrival": { "recorded_at": "2020-02-02T20:02:02.000Z" } }
        """.trimIndent()

        val marker = moshi.adapter(GeofenceMarker::class.java).fromJson(serializedMarker)
            ?: throw NullPointerException("No marker deserialized")

        assertEquals("2020-02-02T20:02:02.000Z", marker.arrival?.recordedAt)
        assertEquals("42-42", marker.geofenceId)

    }

}