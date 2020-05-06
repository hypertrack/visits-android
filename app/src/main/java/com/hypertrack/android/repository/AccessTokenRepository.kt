package com.hypertrack.android.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.hypertrack.android.AUTH_HEADER_KEY
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AccessTokenRepository(
    private val publishableKey : String,
    private val deviceId : String,
    private var lastToken : String?
) {
    fun getAccessToken(): String = lastToken?:refreshToken()

    fun refreshToken(): String {
        Log.d(TAG, "Refreshing token $lastToken with key $publishableKey for deviceId $deviceId")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://live-api.htprod.hypertrack.com/authenticate")
            .header(AUTH_HEADER_KEY, Credentials.basic(publishableKey, ""))
            .post("""{"device_id": "$deviceId"}""".toRequestBody(MEDIA_TYPE_JSON))
            .build()

        client
            .newCall(request)
            .execute().use {
            response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Failed to refresh token $response")
                lastToken = ""

            } else {
                response.body?.let {
                    val string = it.string()
                    val responseObject = Gson().fromJson(string, AuthCallResponse::class.java)
                    lastToken = responseObject.accessToken
                }
            }
        }
        Log.d(TAG, "Got token $lastToken" )
        return lastToken ?: ""
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
        const val TAG = "AccessTokenRepo"
    }
}

private data class AuthCallResponse(@SerializedName("access_token") val accessToken:String, @SerializedName("expires_in") val expiresIn: Int)