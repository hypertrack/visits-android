package com.hypertrack.android.api

import com.hypertrack.android.AUTH_HEADER_KEY
import com.hypertrack.android.repository.BasicAuthAccessTokenRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class AccessTokenTest {

    companion object {
        const val PUBLISHABLE_KEY = "uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw"
        const val DEVICE_ID = "42"
        const val NEW_JWT_TOKEN = "new.jwt.token"
    }

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start()
    }

    @Test
    fun itShouldRequestNewTokenIfLastSavedIsNull() {
        val accessTokenRepository =
            BasicAuthAccessTokenRepository(
                mockWebServer.authUrl(),
                DEVICE_ID,
                PUBLISHABLE_KEY
            )
        enqueueAuthResponse()

        val token = accessTokenRepository.getAccessToken()
        Assert.assertTrue(token.isNotEmpty())
    }

    private fun enqueueAuthResponse() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("""{"access_token":"$NEW_JWT_TOKEN","expires_in":42}""")
        )
    }

    @Test
    fun itShouldUseLastTokenIfPresent() {

        val oldToken = "old.JWT.token"
        val accessTokenRepository =
            BasicAuthAccessTokenRepository(
                mockWebServer.authUrl(),
                DEVICE_ID,
                PUBLISHABLE_KEY,
                token = oldToken
            )
        enqueueAuthResponse()
        val token = accessTokenRepository.getAccessToken()

        Assert.assertEquals(oldToken, token)

    }

    @Test
    fun itShouldUseRefreshLastTokenIfRequested() {

        val oldToken = "old.JWT.token"
        val accessTokenRepository =
            BasicAuthAccessTokenRepository(
                mockWebServer.authUrl(),
                DEVICE_ID,
                PUBLISHABLE_KEY,
                token = oldToken
            )
        enqueueAuthResponse()
        val token = accessTokenRepository.refreshToken()

        Assert.assertNotEquals(oldToken, token)

    }

    @Test
    fun itShouldAddRequestTokenHeaderToRequests() {

        val lastToken = "last.JWT.token"
        val client = OkHttpClient.Builder()
            .addInterceptor(
                AccessTokenInterceptor(
                    BasicAuthAccessTokenRepository(
                        mockWebServer.authUrl(),
                        DEVICE_ID,
                        PUBLISHABLE_KEY,
                        token = lastToken
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
        Assert.assertEquals("Bearer $lastToken", authorizationHeader)
        mockWebServer.shutdown()

    }

    @Test
    fun itShouldAddRefreshTokenIfGotHttpUnauthorizedResponseCode() {

        val lastToken = "last.JWT.token"
        val accessTokenRepository =
            BasicAuthAccessTokenRepository(mockWebServer.authUrl(), DEVICE_ID, PUBLISHABLE_KEY, token = lastToken)
        val client = OkHttpClient.Builder()
            .authenticator(
                AccessTokenAuthenticator(
                    accessTokenRepository
                )
            )
            .build()
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED))
        mockWebServer.enqueue(MockResponse())
        mockWebServer.start()
        enqueueAuthResponse()

        client
            .newCall(Request.Builder().url(mockWebServer.url("/")).build())
            .execute()

        val token = accessTokenRepository.getAccessToken()
        Assert.assertEquals(NEW_JWT_TOKEN, token)
        mockWebServer.shutdown()

    }

    @After
    fun tearDown() {
        try {
            mockWebServer.shutdown()
        } catch (ignored: Throwable) {

        }
    }

    private fun MockWebServer.authUrl() = this.url("/authenticate").toString()
}