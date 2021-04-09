@file:Suppress("BlockingMethodInNonBlockingContext")

package com.hypertrack.android.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hypertrack.android.models.History
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.models.MarkerType
import com.hypertrack.android.repository.AccessTokenRepository
import com.squareup.moshi.JsonDataException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.net.HttpURLConnection
import java.time.LocalDate
import java.util.*

@ExperimentalCoroutinesApi
class ApiClientTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val mockWebServer = MockWebServer()
    private lateinit var apiClient: ApiClient

    @Before
    fun setUp() {
        mockWebServer.start()
        val accessTokenRepo = mockk<AccessTokenRepository>()
        every { accessTokenRepo.getAccessToken() } returns "fake.jwt.token"
        every { accessTokenRepo.refreshToken() } returns "new.jwt.token"
        apiClient = ApiClient(accessTokenRepo, mockWebServer.baseUrl(), DEVICE_ID)
    }

    @Test
    fun `it should send post request to start Url on checkin`() = coroutineScope.runBlockingTest {

        mockWebServer.enqueue(MockResponse())
        // FIXME Denys
        // runBlockingTest in Experimental mode doesn't block for okhttp dispatchers
        runBlocking {
            @Suppress("UNUSED_VARIABLE") val ignored = apiClient.clockIn()
        }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/devices/$DEVICE_ID/start", path)
        assertEquals("POST", request.method)

    }

    @Test
    fun `it should send post request to stop url on checkout`() = runBlockingTest {

        mockWebServer.enqueue(MockResponse())
        runBlocking { apiClient.clockOut() }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/devices/$DEVICE_ID/stop", path)
        assertEquals("POST", request.method)
    }

    @Test
    fun `it should send get request to get list of geofences`() = runBlockingTest {
        val pageTokens = listOf("token1", "token2", null)
//        val pageTokens = listOf(null)
        val responseBodies = pageTokens.map { it?.let { "\"$it\"" } ?: "null" }.map {
            GEOFENCE_RESPONSE.replace("##pagination_token##", it)
        }

        responseBodies.forEach { responseBody ->
            mockWebServer.enqueue(
                MockResponse()
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .setBody(responseBody)
            )
        }
        val geofences = runBlocking { apiClient.getGeofences() }

        pageTokens.forEach { token ->
            val request = mockWebServer.takeRequest()
            val path = request.path
            assertEquals(
                "/client/geofences?include_archived=false&include_markers=true&"
                        + "device_id=$DEVICE_ID&pagination_token=${
                    pageTokens.getOrNull(
                        pageTokens.indexOf(
                            token
                        ) - 1
                    )
                }",
                path
            )
            assertEquals("GET", request.method)
        }

        assertEquals(pageTokens.size * 4, geofences.size)
        assertTrue(geofences.any { it.marker?.markers?.first()?.arrival?.recordedAt != null })
    }

    @Test
    fun `it should send get request to get list of trips`() = runBlockingTest {
        val responseBody =
                """
            {
               "pagination_token" : null,
               "links" : {},
               "data" : [
                  {
                     "device_id" : "42",
                     "analytics" : {},
                     "completed_at" : null,
                     "status" : "active",
                     "metadata" : {
                        "reason" : "api-test"
                     },
                     "device_info" : {
                        "sdk_version" : "4.3.1",
                        "os_version" : "10"
                     },
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/0201c34b-53a8-4a4d-9c3d-cd28effd36e3?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmnt3kpsc"
                     },
                     "trip_id" : "0201c34b-53a8-4a4d-9c3d-cd28effd36e3",
                     "summary" : null,
                     "started_at" : "2020-09-15T07:40:32.643897Z"
                  },
                  {
                     "estimate" : {
                        "arrive_at" : "2020-09-15T09:28:38.176257Z",
                        "route" : {
                           "start_address" : "Sobornyi Ave, 196, Zaporizhzhia, Zaporiz'ka oblast, Ukraine, 69000",
                           "end_address" : "Mykhayla Honcharenka Street, 9, Zaporizhzhia, Zaporiz'ka oblast, Ukraine, 69061",
                           "duration" : 162,
                           "polyline" : {
                              "type" : "LineString",
                              "coordinates" : [
                                 [
                                    35.12218,
                                    47.84857
                                 ],
                                 [
                                    35.1237,
                                    47.84984
                                 ],
                                 [
                                    35.12407,
                                    47.85013
                                 ],
                                 [
                                    35.12415,
                                    47.85009
                                 ],
                                 [
                                    35.12512,
                                    47.84959
                                 ],
                                 [
                                    35.12544,
                                    47.8494
                                 ],
                                 [
                                    35.12483,
                                    47.84887
                                 ],
                                 [
                                    35.12445,
                                    47.84856
                                 ],
                                 [
                                    35.12416,
                                    47.84833
                                 ],
                                 [
                                    35.12363,
                                    47.84791
                                 ]
                              ]
                           },
                           "distance" : 569,
                           "remaining_duration" : 162
                        },
                        "reroutes_exceeded" : false
                     },
                     "eta_relevance_data" : {
                        "status" : true
                     },
                     "device_id" : "42",
                     "device_info" : {
                        "os_version" : "9",
                        "sdk_version" : "4.6.0-SNAPSHOT"
                     },
                     "status" : "active",
                     "completed_at" : null,
                     "analytics" : {},
                     "summary" : null,
                     "trip_id" : "4ee5713c-250e-4094-b6c6-7c7ae33717b6",
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/4ee5713c-250e-4094-b6c6-7c7ae33717b6?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmntkgzcj"
                     },
                     "started_at" : "2020-09-15T07:54:02.305516Z",
                     "destination" : {
                        "geometry" : {
                           "type" : "Point",
                           "coordinates" : [
                              35.1235317438841,
                              47.847959440945
                           ]
                        },
                        "address" : "Mykhayla Honcharenka Street, 9, Zaporizhzhia, Zaporiz'ka oblast, Ukraine, 69061",
                        "scheduled_at" : null,
                        "radius" : 30
                     }
                  },
                  {
                     "device_id" : "42",
                     "eta_relevance_data" : {
                        "status" : true
                     },
                     "estimate" : {
                        "arrive_at" : "2020-09-15T09:12:23.584444Z",
                        "route" : {
                           "polyline" : {
                              "type" : "LineString",
                              "coordinates" : [
                                 [
                                    -122.50384,
                                    37.761
                                 ],
                                 [
                                    -122.50393,
                                    37.76239
                                 ],
                                 [
                                    -122.50428,
                                    37.76237
                                 ],
                                 [
                                    -122.50457,
                                    37.76235
                                 ],
                                 [
                                    -122.50607,
                                    37.76229
                                 ],
                                 [
                                    -122.50822,
                                    37.7622
                                 ],
                                 [
                                    -122.50965,
                                    37.76213
                                 ],
                                 [
                                    -122.50966,
                                    37.76221
                                 ]
                              ]
                           },
                           "distance" : 668,
                           "remaining_duration" : 137,
                           "start_address" : "1374 44th Ave, San Francisco, CA 94122, USA",
                           "end_address" : "1300 Great Hwy, San Francisco, CA 94122, USA",
                           "duration" : 137
                        },
                        "reroutes_exceeded" : false
                     },
                     "completed_at" : null,
                     "analytics" : {},
                     "device_info" : {
                        "sdk_version" : "4.4.0",
                        "os_version" : "13.5.1"
                     },
                     "status" : "active",
                     "summary" : null,
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/6f6d89eb-6b0e-444f-bf32-8601d488c69b?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmntdawy3"
                     },
                     "trip_id" : "6f6d89eb-6b0e-444f-bf32-8601d488c69b",
                     "destination" : {
                        "radius" : 30,
                        "scheduled_at" : null,
                        "address" : "1300 Great Hwy, San Francisco, CA 94122, USA",
                        "geometry" : {
                           "coordinates" : [
                              -122.509639,
                              37.762207
                           ],
                           "type" : "Point"
                        }
                     },
                     "started_at" : "2020-09-15T07:51:38.828420Z"
                  },
                  {
                     "device_id" : "42",
                     "completed_at" : null,
                     "analytics" : {},
                     "device_info" : {
                        "os_version" : "10",
                        "sdk_version" : "4.3.1"
                     },
                     "metadata" : {
                        "reason" : "api-test"
                     },
                     "status" : "active",
                     "summary" : null,
                     "views" : {
                        "embed_url" : "https://embed.hypertrack.com/trips/9b5c5fa6-4f77-4ed6-b03f-4e19c238b8f0?publishable_key=uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                        "share_url" : "https://trck.at/mmmntqv5qd"
                     },
                     "trip_id" : "9b5c5fa6-4f77-4ed6-b03f-4e19c238b8f0",
                     "started_at" : "2020-09-15T07:58:07.937579Z"
                  }
               ]
            }
            """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody))
        val trips = runBlocking { apiClient.getTrips() }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/trips?device_id=$DEVICE_ID&pagination_token=", path)
        assertEquals("GET", request.method)

        assertEquals(2, trips.size)
    }

    @Test
    fun `it should send get request to get device history`() = runBlockingTest {
        val responseBody =
                """
        {
            "locations": { "type": "LineString", "coordinates": [] },
            "markers": [],
            "device_id": "832C9D60-CF83-3217-B150-B62017B192B4",
            "started_at": "2021-02-16T22:00:00.000Z",
            "completed_at": "2021-02-17T22:00:00.000Z",
            "distance": 0,
            "duration": 86400,
            "tracking_rate": 0,
            "inactive_reasons": [],
            "inactive_duration": 0,
            "active_duration": 0,
            "stop_duration": 0,
            "walk_duration": 0,
            "trips": 0,
            "geotags": 0,
            "geofences_visited": 0
        }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody))
        val historyResult = runBlocking {
            apiClient.getHistory(
                    LocalDate.of(2020, 2, 5),
                    TimeZone.getTimeZone("America/Los_Angeles").toZoneId()
            )
        }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/devices/$DEVICE_ID/history/2020-02-05?timezone=America%2FLos_Angeles", path)
        assertEquals("GET", request.method)
        assertTrue(historyResult is History)

    }

    @Test
    fun `it should receive distance and insights from device history`() = runBlockingTest {
        val responseBody =
                """
        {
            "device_id": "A24BA1B4-3B11-36F7-8DD7-15D97C3FD912",
            "locations": {"type": "LineString", "coordinates": []},
            "markers": [ ],
            "started_at": "2021-02-16T22:00:00.000Z",
            "completed_at": "2021-02-17T22:00:00.000Z",
            "distance": 6347.0,
            "duration": 86400,
            "tracking_rate": 100.0,
            "inactive_reasons": [],
            "inactive_duration": 0,
            "active_duration": 158,
            "stop_duration": 0,
            "drive_duration": 610,
            "walk_duration": 0,
            "trips": 0,
            "geotags": 0,
            "geofences_visited": 0
        }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody))
        val historyResult = runBlocking {
            apiClient.getHistory(
                    LocalDate.of(2020, 2, 5),
                    TimeZone.getTimeZone("America/Los_Angeles").toZoneId()
            )
        }

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(historyResult is History)
        with(historyResult as History) {

            assertEquals(6347, summary.totalDistance)
            assertEquals(6347, summary.totalDriveDistance)
            assertEquals(86400, summary.totalDuration)
            assertEquals(610, summary.totalDriveDuration)
        }

    }

    @Test
    fun `it should receive location data points from device history`() = runBlockingTest {
        val responseBody =
                """
            {
               "device_id" : "A24BA1B4-3B11-36F7-8DD7-15D97C3FD912",
               "locations" : {
                  "coordinates" : [
                       [ -122.397368, 37.792382, 42.0,  "2021-02-05T11:53:10.544Z" ],
                       [ -122.39737,  37.79238,  42.42, "2021-02-05T11:53:10.544Z" ],
                       [ -122.39737,  37.79238,  null,  "2021-02-05T11:53:18.942Z" ],
                       [ -122.39737,  37.79238,  null,  "2021-02-05T11:53:24.247Z" ],
                       [ -122.39737,  37.79238,  null,  "2021-02-05T11:53:29.259Z" ]
                  ],
                  "type" : "LineString"
               },
            "markers": [ ],
            "started_at": "2021-02-16T22:00:00.000Z",
            "completed_at": "2021-02-17T22:00:00.000Z",
            "distance": 1007.0,
            "duration": 86400,
            "tracking_rate": 100.0,
            "inactive_reasons": [],
            "inactive_duration": 0,
            "active_duration": 158,
            "stop_duration": 0,
            "drive_duration": 158,
            "walk_duration": 0,
            "trips": 0,
            "geotags": 0,
            "geofences_visited": 0
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody))
        val historyResult = runBlocking {
            apiClient.getHistory(
                    LocalDate.of(2020, 2, 5),
                    TimeZone.getTimeZone("America/Los_Angeles").toZoneId()
            )
        }

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(historyResult is History)
        val history = historyResult as History
        with(history.locationTimePoints) {
            assertEquals(5, size)
            assertEquals(-122.397368, this[0].first.longitude, 0.000001)
            assertEquals(37.792382, this[0].first.latitude, 0.000001)
            assertEquals("2021-02-05T11:53:10.544Z", this[0].second)
            assertEquals(-122.39737, this[3].first.longitude, 0.000001)
            assertEquals("2021-02-05T11:53:29.259Z", this[4].second)
        }
    }

    @Test
    fun `it should receive status markers from device history`() = runBlockingTest {
        val responseBody =
                """
            {
               "markers" : [
                    {
                         "marker_id" : "8b6aeb0f-1a8f-4900-95ee-03755ba21015",
                         "data" : {
                            "value" : "inactive",
                            "end" : {
                               "recorded_at" : "2021-02-05T11:53:10.544Z",
                               "location" : {
                                  "geometry" : {
                                     "coordinates" : [ -122.397368, 37.792382 ],
                                     "type" : "Point"
                                  },
                                  "recorded_at" : "2021-02-05T11:53:10.544Z"
                               }
                            },
                            "start" : {
                               "recorded_at" : "2021-02-05T00:00:00+00:00",
                               "location" : {
                                  "geometry" : {
                                     "type" : "Point",
                                     "coordinates" : [ -122.397368, 37.792382 ]
                                  },
                                  "recorded_at" : "2021-02-05T11:53:10.544Z"
                               }
                            },
                            "duration" : 42791,
                            "reason" : "stopped_programmatically"
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
                                    "coordinates" : [-122.4249, 37.7599 ],
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
                ],
               "device_id" : "A24BA1B4-3B11-36F7-8DD7-15D97C3FD912",
               "completed_at" : "2021-02-05T22:00:00.000Z",
               "locations" : {
                  "coordinates" : [],
                  "type" : "LineString"
               },
            "started_at": "2021-02-16T22:00:00.000Z",
            "completed_at": "2021-02-17T22:00:00.000Z",
            "distance": 1007.0,
            "duration": 86400,
            "tracking_rate": 100.0,
            "inactive_reasons": [],
            "inactive_duration": 0,
            "active_duration": 158,
            "stop_duration": 0,
            "drive_duration": 158,
            "walk_duration": 0,
            "trips": 0,
            "geotags": 0,
            "geofences_visited": 0
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody))
        val historyResult = runBlocking {
            apiClient.getHistory(
                    LocalDate.of(2020, 2, 5),
                    TimeZone.getTimeZone("America/Los_Angeles").toZoneId()
            )
        }

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(historyResult is History)
        val history = historyResult as History
        assertEquals(3, history.markers.size)
        with(history.markers.first()) {
            assertEquals(MarkerType.STATUS, type)
            assertEquals("2021-02-05T00:00:00+00:00", timestamp)
            assertEquals(-122.397368, location!!.longitude, 0.000001)
            assertEquals(37.792382, location!!.latitude, 0.000001)
        }

        with(history.markers[1]) {
            assertEquals(MarkerType.GEOTAG, type)
            assertEquals("2021-02-03T08:50:06.757Z", timestamp)
            assertEquals(-122.084, location!!.longitude, 0.000001)
            assertEquals(37.421998, location!!.latitude, 0.000001)
        }

        with(history.markers.last()) {
            assertEquals(MarkerType.GEOFENCE_ENTRY, type)
            assertEquals("2021-02-05T12:11:37.838Z", timestamp)
            assertEquals(-122.4249, location!!.longitude, 0.000001)
            assertEquals(37.7599, location!!.latitude, 0.000001)
        }
    }

    @Test
    fun `it should return history error if 500 status was received on history endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))
        val historyResult = runBlocking {
            apiClient.getHistory(
                    LocalDate.of(2020, 2, 5),
                    TimeZone.getTimeZone("America/Los_Angeles").toZoneId()
            )
        }

        assertTrue(historyResult is HistoryError)
    }

    @Test
    fun `it should return history error if invalid body history was received`() {
        val body = """{"error": "Internal server error"}"""
        mockWebServer.enqueue(MockResponse().setBody(body))
        val historyResult = runBlocking {
            apiClient.getHistory(
                    LocalDate.of(2020, 2, 5),
                    TimeZone.getTimeZone("America/Los_Angeles").toZoneId()
            )
        }

        assertTrue(historyResult is HistoryError)
        assertTrue((historyResult as HistoryError).error is JsonDataException)
    }

    @After
    fun tearDown() {
        try {
            mockWebServer.shutdown()
        } catch (ignored: Throwable) {

        }
    }

    private fun MockWebServer.baseUrl() = this.url("/").toString()

    companion object {
        const val DEVICE_ID = "42"

        val GEOFENCE_RESPONSE = """
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
                        "archived": false,
                        "geofence_type": "device",
                        "radius": 30
                    },
                    {
                        "geofence_id": "41085c46-191d-44cd-8366-130be17d8796",
                        "account_id": "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                        "device_id": "86BB603D-B905-367D-AE3B-3ECFA4428D96",
                        "single_use": false,
                        "created_at": "2020-01-27T14:33:59.188520+00:00",
                        "metadata": { "destination": true },
                        "geometry": {
                            "type": "Point",
                            "coordinates": [ 35.1046979427338, 47.8588572595771 ]
                        },
                        "markers" : {
                            "links" : { "next" : null },
                            "data" : [
                               {
                                  "created_at" : "2021-01-04T09:22:51.950Z",
                                  "geofence_type" : "device",
                                  "geofence_id" : "41085c46-191d-44cd-8366-130be17d8796",
                                  "marker_id" : "7aeb9656-510a-4d91-85b0-6865ea6f39ed",
                                  "trip_id" : null,
                                  "account_id" : "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                                  "metadata": { "destination": true },
                                  "geometry": {
                                      "type": "Point",
                                      "coordinates": [ 35.1046979427338, 47.8588572595771 ]
                                  },
                                  "duration" : 334,
                                  "route_to" : null,
                                  "device_id" : "DC3383D1-0EB2-38B2-B80F-3926C580DD35",
                                  "geofence_metadata" : {
                                     "device_geofence" : true,
                                     "destination": true
                                  },
                                  "arrival" : {
                                     "recorded_at" : "2021-01-04T09:22:48.692Z",
                                     "location" : {
                                        "type" : "Point",
                                        "coordinates" : [ -122.393237, 37.794587 ]
                                     }
                                  },
                                  "exit" : {
                                     "location" : null,
                                     "recorded_at" : "2021-01-04T09:28:22.902Z"
                                  }
                               }
                            ],
                            "pagination_token" : null
                         },
                        "archived": false,
                        "geofence_type": "device",
                        "radius": 30
                    },
                    {
                        "geofence_id": "2c1f2901-c5a5-43f6-a29e-33e58ca9a19e",
                        "account_id": "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                        "device_id": "86BB603D-B905-367D-AE3B-3ECFA4428D96",
                        "single_use": false,
                        "created_at": "2020-02-21T17:51:06.415161+00:00",
                        "metadata": { "location": "Ferry Building" },
                        "geometry": {
                            "type": "Point",
                            "coordinates": [ -122.394, 37.7957 ]
                        },
                        "archived": false,
                        "geofence_type": "device",
                        "radius": 50
                    },
                    {
                        "geofence_id": "4aca5fb7-bdab-46b9-a691-1b258a16391b",
                        "account_id": "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                        "device_id": "86BB603D-B905-367D-AE3B-3ECFA4428D96",
                        "single_use": false,
                        "created_at": "2020-11-26T14:48:11.727418+00:00",
                        "metadata": { "location": "Ferry Building" },
                        "geometry": {
                            "type": "Point",
                            "coordinates": [ -122.394, 37.7957 ]
                        },
                        "archived": false,
                        "geofence_type": "device",
                        "radius": 50
                    }
                ],
                "pagination_token": ##pagination_token##,
                "links": { "next": null }
            }
            """.trimIndent()
    }

}

@ExperimentalCoroutinesApi
class MainCoroutineScopeRule(private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
        TestWatcher(),
        TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        // If your codebase allows the injection of other dispatchers like
        // Dispatchers.Default and Dispatchers.IO, consider injecting all of them here
        // and renaming this class to `CoroutineScopeRule`
        //
        // All injected dispatchers in a test should point to a single instance of
        // TestCoroutineDispatcher.
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}
