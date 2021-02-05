package com.hypertrack.android.api

import android.graphics.Bitmap
import android.util.Log
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.utils.Injector
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ApiClient(
    accessTokenRepository: AccessTokenRepository,
    baseUrl:String,
    private val deviceId: String
) {

    @Suppress("unused")
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    val api: ApiInterface = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(Injector.getMoshi()))
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
//                .addInterceptor(loggingInterceptor)
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
            throw e
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
            throw e
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
            throw e
        }
        return ""
    }

    suspend fun getHistory(day: LocalDate, timezone: ZoneId): HistoryResult {
        try {
            with(api.getHistory(deviceId, day.format(DateTimeFormatter.ISO_LOCAL_DATE), timezone.id)) {
                if (isSuccessful) return body()?: HistoryError(null)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Got exception $e fetching device history")
            return HistoryError(e)
        }
        return HistoryError(null)
    }

    companion object { const val TAG = "ApiClient"}

}

