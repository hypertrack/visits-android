@file:Suppress("BlockingMethodInNonBlockingContext")

package com.hypertrack.android.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hypertrack.android.TestMockData
import com.hypertrack.android.models.History
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.models.MarkerType
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.utils.MockData
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
        apiClient =
            ApiClient(accessTokenRepo, mockWebServer.baseUrl(), DEVICE_ID, Injector.getMoshi())
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
    fun `it should send get request to get list of trips`() = runBlockingTest {
        val responseBody = TestMockData.MOCK_TRIPS_JSON
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
        val responseBody = TestMockData.MOCK_HISTORY_JSON

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
