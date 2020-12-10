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

    suspend fun getGeofences(page:  String = "") : List<Geofence> {
        try {
            val response = api.getGeofences(deviceId, page)
            if (response.isSuccessful) {
                val geofences: Collection<Geofence> = response.body()?.geofences ?: return emptyList()
                if (geofences.isEmpty()) return emptyList()
                val idsToCheck  = geofences.map { it.geofence_id }.toMutableList()
                val arrivals = mutableMapOf<String, String>()
                var nextPageOfMarkers:String? = null
                do {
                    val markersResponse =  api.getGeofenceMarkers(deviceId, nextPageOfMarkers?:"")
                    if (markersResponse.isSuccessful) {
                        markersResponse.body()?.markers?.let { it.forEach {marker ->
                                if (!arrivals.keys.contains(marker.geofenceId)) {
                                    val recordedAt = marker.arrival?.recordedAt
                                    recordedAt?.let {
                                        arrivals[marker.geofenceId] = recordedAt
                                        idsToCheck.remove(marker.geofenceId)
                                    }
                                }
                            }
                        }
                        nextPageOfMarkers = markersResponse.body()?.next
                    }

                } while (idsToCheck.isNotEmpty() && nextPageOfMarkers != null)

                return geofences.map {
                    if (arrivals.keys.contains(it.geofence_id)) {
                        it.visitedAt = arrivals[it.geofence_id]?:""
                    }
                    it
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Got exception while fetching geofences $e")
            throw Exception(e)
        }
        return emptyList()
    }

    suspend fun getTrips(page:  String = ""): List<Trip> {
        try {
            val response = api.getTrips(deviceId, page)
            if (response.isSuccessful) {
                Log.v(TAG, "Got response ${response.body()}")
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

    companion object { const val TAG = "ApiClient"}

}

