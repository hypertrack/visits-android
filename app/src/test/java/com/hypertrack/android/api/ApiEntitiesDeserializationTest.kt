package com.hypertrack.android.api

import com.hypertrack.android.utils.Injector
import com.squareup.moshi.Types
import org.junit.Assert.*
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
                                "marker_id": "42",
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
        val geofences = geofenceResponse?.geofences
                ?: throw NullPointerException("no geofences in response")
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
                                        "marker_id": "42",
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
        assertEquals("42", geofence.marker!!.markers.first().markerId)
        assertEquals("010b7861-59fc-4157-9fcd-6d2e0c5072d9", geofence.geofence_id)
        assertEquals("2020-01-16T12:51:00.010934+00:00", geofence.created_at)
        assertEquals(30, geofence.radius)
        assertEquals("2020-02-02T20:02:02.000Z", geofence.visitedAt)

    }

    @Test
    fun `it should deserialize markers correctly`() {
        val serializedMarker = """ 
            { "marker_id": "42", "geofence_id": "42-42", "arrival": { "recorded_at": "2020-02-02T20:02:02.000Z" } }
        """.trimIndent()

        val marker = moshi.adapter(GeofenceMarker::class.java).fromJson(serializedMarker)
                ?: throw NullPointerException("No marker deserialized")

        assertEquals("2020-02-02T20:02:02.000Z", marker.arrival?.recordedAt)
        assertEquals("42-42", marker.geofenceId)

    }

    @Test
    fun `it should deserialize time series object`() {
        val serializedLocations = """
            {
              "type" : "LineString",
              "coordinates" : [
                 [ -122.084009, 37.421986, null, "2021-02-03T07:46:31.021Z" ],
                 [ -122.084009, 37.421986, null, "2021-02-03T07:46:31.021Z" ],
                 [ -122.084009, 37.421986, null, "2021-02-03T07:46:31.021Z" ],
                 [ -122.084009, 37.421986, null, "2021-02-03T07:52:34.428Z" ]                      
              ]
            }
      """

        val locations = moshi.adapter(Locations::class.java).nullSafe().fromJson(serializedLocations)!!
        with(locations) {
            assertEquals("LineString", type)
            assertEquals(4, coordinates.size)
            assertEquals(-122.084009, coordinates[0].longitude, 0.000001)
            assertEquals(37.421986, coordinates[1].latitude, 0.000001)
            assertNull(coordinates[2].altitude)
            assertEquals("2021-02-03T07:52:34.428Z", coordinates[3].timestamp)
        }

    }

    @Test
    fun `it should deserialize device history markers`() {
        val serializedMarkers = """
            [
                  {
                     "marker_id" : "d0879f89-69fd-4227-a07e-65924b323c69",
                     "data" : {
                        "value" : "inactive",
                        "start" : {
                           "recorded_at" : "2021-02-03T00:00:00+00:00",
                           "location" : {
                              "geometry" : {
                                 "type" : "Point",
                                 "coordinates" : [ -122.084009, 37.421986 ]
                              },
                              "recorded_at" : "2021-02-03T07:46:31.021Z"
                           }
                        },
                        "duration" : 27991,
                        "reason" : "stopped_programmatically",
                        "end" : {
                           "location" : {
                              "recorded_at" : "2021-02-03T07:46:31.021Z",
                              "geometry" : {
                                 "coordinates" : [ -122.084009, 37.421986 ],
                                 "type" : "Point"
                              }
                           },
                           "recorded_at" : "2021-02-03T07:46:31.021Z"
                        }
                     },
                     "type" : "device_status"
                  },
                  {
                     "data" : {
                        "metadata" : {
                           "type" : "Test geotag at 1612342206755"
                        },
                        "location" : {
                           "type" : "Point",
                           "coordinates" : [ -122.084, 37.421998, 5 ]
                        },
                        "recorded_at" : "2021-02-03T08:50:06.757Z"
                     },
                     "type" : "trip_marker",
                     "marker_id" : "b05df9e8-8f91-44eb-b01f-bacfa59b4349"
                  },
                    {
                        "marker_id" : "5eb13571-d3cc-494d-966e-1cc5759ba965",
                        "type" : "geofence",
                        "data" : {
                           "exit" : {
                              "location" : {
                                 "geometry" : null,
                                 "recorded_at" : "2021-02-05T12:18:20.986Z"
                              }
                           },
                           "duration" : 403,
                           "arrival" : {
                              "location" : {
                                 "geometry" : {
                                    "coordinates" : [
                                       -122.4249,
                                       37.7599
                                    ],
                                    "type" : "Point"
                                 },
                                 "recorded_at" : "2021-02-05T12:11:37.838Z"
                              }
                           },
                           "geofence" : {
                              "metadata" : {
                                 "name" : "Mission Dolores Park"
                              },
                              "geometry" : {
                                 "coordinates" : [
                                    -122.426366,
                                    37.761115
                                 ],
                                 "type" : "Point"
                              },
                              "geofence_id" : "8b63f7d3-4ba4-4dbf-b100-0c843445d5b2",
                              "radius" : 200
                           }
                        }
                     }                  
            ]
        """.trimIndent()

        val markers = moshi
                .adapter<List<HistoryMarker>>(Types.newParameterizedType(List::class.java, HistoryMarker::class.java))
                .fromJson(serializedMarkers)!!

        assertEquals(3, markers.size)

    }

    @Test
    fun `it should deserialize device history marker`() {
        val serializedMarker = """{
                     "marker_id" : "d0879f89-69fd-4227-a07e-65924b323c69",
                     "data" : {
                        "value" : "inactive",
                        "start" : {
                           "recorded_at" : "2021-02-03T00:00:00+00:00",
                           "location" : {
                              "geometry" : {
                                 "type" : "Point",
                                 "coordinates" : [ -122.084009, 37.421986 ]
                              },
                              "recorded_at" : "2021-02-03T07:46:31.021Z"
                           }
                        },
                        "duration" : 27991,
                        "reason" : "stopped_programmatically",
                        "end" : {
                           "location" : {
                              "recorded_at" : "2021-02-03T07:46:31.021Z",
                              "geometry" : {
                                 "coordinates" : [ -122.084009, 37.421986 ],
                                 "type" : "Point"
                              }
                           },
                           "recorded_at" : "2021-02-03T07:46:31.021Z"
                        }
                     },
                     "type" : "device_status"
                  }
        """.trimIndent()

        val marker = moshi
                .adapter(HistoryMarker::class.java)
                .fromJson(serializedMarker)!!
        assertTrue(marker is HistoryStatusMarker)
        with(marker as HistoryStatusMarker) {
            assertEquals("d0879f89-69fd-4227-a07e-65924b323c69", markerId)
            assertEquals("inactive", data.value)
            assertEquals(27991, data.duration)
            assertEquals("2021-02-03T00:00:00+00:00", data.start.recordedAt)
            assertEquals("2021-02-03T07:46:31.021Z", data.end.recordedAt)
        }

    }
}