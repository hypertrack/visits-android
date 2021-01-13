package com.hypertrack.android.api

import android.graphics.Bitmap
import android.util.Log
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.utils.Injector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    accessTokenRepository: AccessTokenRepository,
    baseUrl:String,
    private val deviceId: String
) {

    val api: ApiInterface = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(Injector.getMoshi()))
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
                .addInterceptor(UserAgentInterceptor())
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .build().create(ApiInterface::class.java)

    suspend fun clockIn() = api.clockIn(deviceId)

    suspend fun clockOut() = api.clockOut(deviceId)

    suspend fun getGeofences(page:  String = "") : List<Geofence> {
        try {
            val response = api.getGeofences(deviceId, page)
            return if (response.isSuccessful) {
                 response.body()?.geofences?.toList() ?: emptyList()
            } else return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Got exception while fetching geofences $e")
            throw Exception(e)
        }
    }

    suspend fun getTrips(page:  String = ""): List<Trip> {
        try {
            val response = api.getTrips(deviceId, page)
            if (response.isSuccessful) {
                // Log.v(TAG, "Got response ${response.body()}")
                return response.body()?.trips?.filterNot {
                    it.destination == null || it.tripId.isNullOrEmpty()
                }
                    ?: emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Got exception while trying to refresh trips $e")
            throw Exception(e)
        }
        return emptyList()



    }

    suspend fun uploadImage(image: Bitmap) : String {
        try {
            val response = api.persistImage(deviceId, EncodedImage(image))
            if (response.isSuccessful) {
                // Log.v(TAG, "Got post image response ${response.body()}")
                return response.body()?.name ?: ""
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Got exception $e uploading image")
            throw Exception(e)
        }
        return ""
    }

    companion object { const val TAG = "ApiClient"}

}

