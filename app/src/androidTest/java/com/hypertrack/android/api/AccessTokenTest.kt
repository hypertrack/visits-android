package com.hypertrack.android.api

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.android.AUTH_HEADER_KEY
import com.hypertrack.android.AUTH_URL
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.sdk.HyperTrack
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class AccessTokenTest {

    companion object {
        const val PUBLISHABLE_KEY = "uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw"
        const val TAG = "AccessTokenTest"
    }

    private val context: Context?
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var hyperTrack: HyperTrack

    @Before
    fun setUp() {
        hyperTrack = HyperTrack.getInstance(context, PUBLISHABLE_KEY)
        Log.d(TAG, "HyperTrack initialized with device id ${hyperTrack.deviceID}")
    }

    @Test
    fun itShouldRequestNewTokenIfLastSavedIsNull() {
        val accessTokenRepository = AccessTokenRepository(
            AUTH_URL,
            hyperTrack.deviceID,
            null,
            PUBLISHABLE_KEY
        )

        val token = accessTokenRepository.getAccessToken()
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun itShouldUseLastTokenIfPresent() {

        val oldToken = "old.JWT.token"
        val accessTokenRepository = AccessTokenRepository(
            AUTH_URL,
            hyperTrack.deviceID,
            oldToken,
            PUBLISHABLE_KEY
        )
        val token = accessTokenRepository.getAccessToken()

        assertEquals(oldToken, token)

    }

    @Test
    fun itShouldUseRefreshLastTokenIfRequested() {

        val oldToken = "old.JWT.token"
        val accessTokenRepository = AccessTokenRepository(
            AUTH_URL,
            hyperTrack.deviceID,
            oldToken,
            PUBLISHABLE_KEY
        )
        val token = accessTokenRepository.refreshToken()

        assertNotEquals(oldToken, token)

    }

    @Test
    fun itShouldAddRequestTokenHeaderToRequests() {

        val lastToken = "last.JWT.token"
        val client = OkHttpClient.Builder()
            .addInterceptor(
                AccessTokenInterceptor(
                    AccessTokenRepository(
                        AUTH_URL,
                        hyperTrack.deviceID,
                        lastToken,
                        PUBLISHABLE_KEY
                    )
                )
            )
            .build()
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(MockResponse())
        mockWebServer.start()

        client
            .newCall(Request.Builder().url(mockWebServer.url("/")).build())
            .execute()
        val recordedRequest = mockWebServer.takeRequest()

        val headers = recordedRequest.headers
        val authorizationHeader = headers[AUTH_HEADER_KEY]?:""
        assertEquals("Bearer $lastToken" , authorizationHeader)
        mockWebServer.shutdown()

    }

    @Test
    fun itShouldAddRefreshTokenIfGotHttpUnauthorizedResponseCode() {

        val lastToken = "last.JWT.token"
        val accessTokenRepository = AccessTokenRepository(
            AUTH_URL,
            hyperTrack.deviceID,
            lastToken,
            PUBLISHABLE_KEY
        )
        val client = OkHttpClient.Builder()
            .authenticator(
                AccessTokenAuthenticator(accessTokenRepository)
            )
            .build()
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED))
        mockWebServer.enqueue(MockResponse())
        mockWebServer.start()

        client
            .newCall(Request.Builder().url(mockWebServer.url("/")).build())
            .execute()

        val token = accessTokenRepository.getAccessToken()
        assertNotEquals(lastToken, token)
        mockWebServer.shutdown()

    }
}
