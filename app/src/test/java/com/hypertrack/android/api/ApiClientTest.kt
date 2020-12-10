@file:Suppress("BlockingMethodInNonBlockingContext")

package com.hypertrack.android.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import okhttp3.mockwebserver.MockWebServer
import com.hypertrack.android.repository.AccessTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class ApiClientTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    companion object {
        const val DEVICE_ID = "42"
        const val GEOFENCES =
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
                "pagination_token": null,
                "links": { "next": null }
            }
            """
        const val TRIPS = 
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
            """
        const val MARKERS =
            """
            {
                "data": [
                    {
                        "marker_id": "d7fdf8e2-9bb2-427c-8a6f-39be15874cbf",
                        "device_id": "86BB603D-B905-367D-AE3B-3ECFA4428D96",
                        "account_id": "1f68e190-af6e-446a-b3f9-d0b1502e63fa",
                        "trip_id": null,
                        "created_at": "2020-12-09T09:11:48.423Z",
                        "arrival": {
                            "recorded_at": "2020-12-09T09:11:45.980Z",
                            "location": {
                                "coordinates": [ -122.393962, 37.795357 ],
                                "type": "Point"
                            }
                        },
                        "exit": null,
                        "geofence_id": "010b7861-59fc-4157-9fcd-6d2e0c5072d9",
                        "metadata": { "location": "Ferry Building" },
                        "geometry": {
                            "type": "Point",
                            "coordinates": [ -122.394, 37.7957 ]
                        },
                        "route_to": {},
                        "geofence_type": "device",
                        "geofence_metadata": {
                            "device_geofence": true,
                            "location": "Ferry Building"
                        },
                        "radius": 50
                    }
                ],
                "pagination_token": null,
                "links": {
                    "next": null
                }
            }
            """
    }
    private val mockWebServer = MockWebServer()
    private lateinit var apiClient : ApiClient

    @Before
    fun setUp() {
        mockWebServer.start()
        val accessTokenRepo = mock(AccessTokenRepository::class.java)
        `when`(accessTokenRepo.getAccessToken())
            .thenReturn("fake.jwt.token")
        `when`(accessTokenRepo.refreshToken())
            .thenReturn("new.jwt.token")
        apiClient = ApiClient(accessTokenRepo, mockWebServer.baseUrl(), DEVICE_ID)
    }
    @Test
    fun itShouldSendPostRequestToStartUrlOnCheckin() = coroutineScope.runBlockingTest {

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
    fun itShouldSendPostRequestToStopUrlOnCheckout() = runBlockingTest {

        mockWebServer.enqueue(MockResponse())
        runBlocking {
            apiClient.clockOut()
        }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/devices/$DEVICE_ID/stop", path)
        assertEquals("POST", request.method)
    }

    @Test
    fun itShouldSendGetRequestToGetListOfGeofences() = runBlockingTest {

        mockWebServer.enqueue(MockResponse().setBody(GEOFENCES))
        mockWebServer.enqueue(MockResponse().setBody(MARKERS))
        val geofences = runBlocking {
            apiClient.getGeofences()
        }


        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/geofences?device_id=$DEVICE_ID&pagination_token=", path)
        assertEquals("GET", request.method)

        assertEquals(4, geofences.size)
    }

    @Test
    fun itShouldSendGetRequestToGetListOfTrips() = runBlockingTest {

        mockWebServer.enqueue(MockResponse().setBody(TRIPS))
        val trips = runBlocking {
            apiClient.getTrips()
        }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/trips?device_id=$DEVICE_ID&pagination_token=", path)
        assertEquals("GET", request.method)

        assertEquals(2, trips.size)
    }

    @After
    fun tearDown() {
        try {
            mockWebServer.shutdown()
        } catch (ignored: Throwable) {

        }
    }

    private fun MockWebServer.baseUrl() = this.url("/").toString()

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
