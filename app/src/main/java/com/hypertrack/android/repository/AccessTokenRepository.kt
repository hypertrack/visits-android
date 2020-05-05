package com.hypertrack.android.repository

import android.util.Log
import com.google.gson.Gson
import com.hypertrack.android.AUTH_HEADER_KEY
import com.hypertrack.android.AUTH_URL_PATH
import com.hypertrack.android.BASE_URL
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
            .url(BASE_URL + AUTH_URL_PATH)
            .header(AUTH_HEADER_KEY, Credentials.basic(publishableKey, ""))
            .post("""{"device_id": "$deviceId"}""".toRequestBody(MEDIA_TYPE_JSON))
            .build()

        client.newCall(request).execute().use {
            response ->
            if (!response.isSuccessful) {
                Log.d(TAG, "Failed to refresh token ${response.message}")
                lastToken = ""

            } else {
                response.body?.let {
                    val responseObject = Gson().fromJson(it.string(), JSONObject::class.java)
                    lastToken = responseObject?.optString("access_token")
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