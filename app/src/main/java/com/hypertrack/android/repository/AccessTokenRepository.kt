package com.hypertrack.android.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.hypertrack.android.AUTH_HEADER_KEY
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

interface AccessTokenRepository {
    fun refreshToken(): String
    fun getAccessToken(): String
}
class BasicAuthAccessTokenRepository(
    private val authUrl: String,
    private val deviceId: String,
    private val userName: String,
    private val userPwd: String = "",
    private var token: String? = null
) : AccessTokenRepository {
    override fun getAccessToken(): String = token?:refreshToken()

    override fun refreshToken(): String {
        Log.v(TAG, "Refreshing token $token for user $userName for deviceId $deviceId")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(authUrl)
            .header(AUTH_HEADER_KEY, Credentials.basic(userName, userPwd))
            .post("""{"device_id": "$deviceId"}""".toRequestBody(MEDIA_TYPE_JSON))
            .build()

        client
            .newCall(request)
            .execute()
            .use { response ->
                token = ""
                if (response.isSuccessful) {
                    response.body?.let {
                        try {
                            val responseObject = Gson().fromJson(it.string(), AuthCallResponse::class.java)
                            token = responseObject.accessToken
                        } catch (ignored: JsonSyntaxException) {
                            Log.w(TAG, "Can't deserialize auth response ${it.string()}")
                        }
                    }
                } else {
                    Log.w(TAG, "Failed to refresh token $response")
                }
        }
        Log.v(TAG, "Updated bearer token $token" )
        return token ?: ""
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
        const val TAG = "AccessTokenRepo"
    }
}

private data class AuthCallResponse(
    @SerializedName("access_token") val accessToken:String,
    @SerializedName("expires_in") val expiresIn: Int
)