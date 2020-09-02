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
        const val VISITS =
            """
                [
                  {
                    "all_devices": false,
                    "created_at": "2020-01-16T12:51:00.010934+00:00",
                    "delete_at": null,
                    "device_id": "f1b28a9c-4163-3003-a683-b77b42b650bf",
                    "device_ids": [ "f1b28a9c-4163-3003-a683-b77b42b650bf" ],
                    "geofence_id": "010b7861-59fc-4157-9fcd-6d2e0c5072d9",
                    "geometry": {
                      "coordinates": [ 35.1047817617655, 47.8586030579782 ],
                      "type": "Point"
                    },
                    "metadata": { "destination": true },
                    "radius": 30,
                    "single_use": false
                  },
                  {
                    "all_devices": false,
                    "created_at": "2020-01-16T15:27:26.586384+00:00",
                    "delete_at": null,
                    "device_id": "f1b28a9c-4163-3003-a683-b77b42b650bf",
                    "device_ids": [ "f1b28a9c-4163-3003-a683-b77b42b650bf" ],
                    "geofence_id": "2e2cfdee-e7dc-4bc7-b9d4-bc6483d40060",
                    "geometry": {
                      "coordinates": [ 35.1241078, 47.8466593 ],
                      "type": "Point"
                    },
                    "metadata": { "destination": true },
                    "radius": 30,
                    "single_use": false
                  },
                  {
                    "all_devices": false,
                    "created_at": "2020-01-27T14:33:59.188520+00:00",
                    "delete_at": null,
                    "device_id": "f1b28a9c-4163-3003-a683-b77b42b650bf",
                    "device_ids": [ "f1b28a9c-4163-3003-a683-b77b42b650bf" ],
                    "geofence_id": "41085c46-191d-44cd-8366-130be17d8796",
                    "geometry": {
                      "coordinates": [ 35.1046979427338, 47.8588572595771 ],
                      "type": "Point"
                    },
                    "metadata": { "destination": true },
                    "radius": 30,
                    "single_use": false
                  },
                  {
                    "all_devices": false,
                    "created_at": "2020-02-21T17:51:06.415161+00:00",
                    "delete_at": null,
                    "device_id": "f1b28a9c-4163-3003-a683-b77b42b650bf",
                    "device_ids": [ "f1b28a9c-4163-3003-a683-b77b42b650bf" ],
                    "geofence_id": "2c1f2901-c5a5-43f6-a29e-33e58ca9a19e",
                    "geometry": {
                      "coordinates": [ 35.1050107553601, 47.8593037965363 ],
                      "type": "Point"
                    },
                    "metadata": { "destination": true },
                    "radius": 30,
                    "single_use": false
                  }
                ]
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
            val ignored = apiClient.checkinCall()
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
            apiClient.checkoutCall()
        }

        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/devices/$DEVICE_ID/stop", path)
        assertEquals("POST", request.method)
    }

    @Test
    fun itShouldSendGetRequestToGetListOfGeofences() = runBlockingTest {

        mockWebServer.enqueue(MockResponse().setBody(VISITS))
        val geofences = runBlocking {
            apiClient.getGeofences()
        }


        val request = mockWebServer.takeRequest()
        val path = request.path
        assertEquals("/client/devices/$DEVICE_ID/geofences", path)
        assertEquals("GET", request.method)

        assertEquals(4, geofences.size)
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
