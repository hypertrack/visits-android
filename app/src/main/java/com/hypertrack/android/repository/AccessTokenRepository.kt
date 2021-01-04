package com.hypertrack.android.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.hypertrack.android.api.UserAgentInterceptor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface AccessTokenRepository {
    fun refreshToken(): String
    fun getAccessToken(): String
    suspend fun refreshTokenAsync() : AccountState
    fun  getConfig() : Any
    val deviceHistoryWebViewUrl: String
}

sealed class AccountState
data class Active(val token: String) : AccountState()
object Suspended : AccountState()
object InvalidCredentials : AccountState()
object Unknown : AccountState()

class BasicAuthAccessTokenRepository(
    private val authUrl: String,
    val deviceId: String,
    private val userName: String,
    private val userPwd: String = "",
    private var token: String? = null
) : AccessTokenRepository {

    private val okHttpClient : OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor(UserAgentInterceptor()).build()
    }

    private val request: Request by lazy {
        Request.Builder()
            .url(authUrl)
            .header(AUTH_HEADER_KEY, Credentials.basic(userName, userPwd))
            .post("""{"device_id": "$deviceId"}""".toRequestBody(MEDIA_TYPE_JSON))
            .build()
    }

    override fun getAccessToken(): String = token?:refreshToken()

    override fun refreshToken(): String {
        // Log.v(TAG, "Refreshing token $token for user $userName for deviceId $deviceId")

        val result = okHttpClient
            .newCall(request)
            .execute()
            .use { response -> getTokenFromResponse(response)}

        return when (result) {
            is Active -> {
                // Log.v(TAG, "Updated bearer token $result.token" )
                token = result.token
                result.token
            }
            else -> ""
        }
    }

    override suspend fun refreshTokenAsync(): AccountState =
        suspendCoroutine { cont ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to get ")
                    cont.resume(Unknown)
                }

                override fun onResponse(call: Call, response: Response) {
                    cont.resume(getTokenFromResponse(response))
                }
            })
        }

    private fun getTokenFromResponse(response: Response) : AccountState {
        // Log.d(TAG, "Getting token from response $response")
        return when {
            response.isSuccessful -> {
                response.body?.let {
                    try {
                        val responseObject = Gson().fromJson(it.string(), AuthCallResponse::class.java)
                        Active(responseObject.accessToken)
                    } catch (ignored: JsonSyntaxException) {
                        Log.w(TAG, "Can't deserialize auth response ${it.string()}")
                        Unknown
                    }
                } ?: Unknown
            }
            response.code == HttpURLConnection.HTTP_FORBIDDEN && response.body?.string()?.contains("trial ended") == true -> {
                Log.w(TAG, "Failed to refresh token $response")
                Suspended
            }
            response.code == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                Log.w(TAG, "Wrong publishable key")
                InvalidCredentials
            }
            else -> {
                Log.w(TAG, "Can't get token from response $response")
                Unknown
            }
        }
    }

    override fun getConfig() : BasicAuthAccessTokenConfig =
        BasicAuthAccessTokenConfig(authUrl, deviceId, userName, userPwd, token)

    override val deviceHistoryWebViewUrl: String
        get() = "https://embed.hypertrack.com/devices/$deviceId?publishable_key=$userName&map_only=true&back=false"

    constructor(
        config: BasicAuthAccessTokenConfig
    ) : this(
        config.authUrl, config.deviceId, config.userName, config.userPwd, config.token
    )

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
        const val TAG = "AccessTokenRepo"
    }
}

private data class AuthCallResponse(
    @SerializedName("access_token") val accessToken:String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class BasicAuthAccessTokenConfig(
    val authUrl: String,
    val deviceId: String,
    val userName: String,
    val userPwd: String = "",
    var token: String? = null
)

const val AUTH_HEADER_KEY = "Authorization"