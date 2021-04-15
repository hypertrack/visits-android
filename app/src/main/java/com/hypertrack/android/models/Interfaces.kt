package com.hypertrack.android.models

import com.hypertrack.android.api.TripParams


interface AbstractBackendProvider : HomeManagementApi, TripManagementApi
interface TripManagementApi {
    suspend fun createTrip(tripParams: TripParams) : ShareableTripResult
    suspend fun completeTrip(tripId: String) : TripCompletionResult
}

interface HomeManagementApi {
    fun getHomeGeofenceLocation(resultHandler: ResultHandler<GeofenceLocation?>)
    fun updateHomeGeofence(homeLocation: GeofenceLocation, resultHandler: ResultHandler<Void?>)
}

interface ResultHandler<T> {
    fun onResult(result: T)
    fun onError(error: Exception)
}

data class GeofenceLocation(val latitude: Double, val longitude: Double)

sealed class ShareableTripResult
class CreateTripError(val error: Throwable?) : ShareableTripResult()
class ShareableTripSuccess(
    val shareUrl: String,
    val embedUrl: String?,
    val tripId: String,
    val remainingDuration: Int?
) : ShareableTripResult()

sealed class TripCompletionResult
class TripCompletionError(val error: Throwable?) : TripCompletionResult()
object TripCompletionSuccess : TripCompletionResult()
