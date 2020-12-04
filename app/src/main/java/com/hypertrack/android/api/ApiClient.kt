package com.hypertrack.android.api

import android.util.Log
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.utils.Injector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ApiClient(
    accessTokenRepository: AccessTokenRepository,
    baseUrl:String,
    private val deviceId: String
) {

    val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(Injector.getGson()))
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
                .addInterceptor(UserAgentInterceptor())
                .build()
        )
        .build().create(ApiInterface::class.java)

    suspend fun clockIn() = api.clockIn(deviceId)

    suspend fun clockOut() = api.clockOut(deviceId)

    suspend fun getGeofences() : List<Geofence> {
        try {
            val response = api.getGeofences(deviceId)
            if (response.isSuccessful) {
                return response.body()?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Got exception while fetching geofences $e")
            throw Exception(e)
        }
        return emptyList()
    }

    suspend fun getTrips(): List<Trip> {
        try {
            val response = api.getTrips(deviceId)
            if (response.isSuccessful)
                Log.v(TAG, "Got response ${response.body()}")
                return response.body()?.trips?.filterNot { it.destination == null || it.tripId.isNullOrEmpty() }?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Got exception while trying to refresh trips $e")
            throw Exception(e)
        }
        return emptyList()



    }

    companion object { const val TAG = "ApiClient"}

}

