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
class ShareableTrip(val shareUrl: String, val embedUrl: String, val tripId: String, val remainingDuration: Int?) : ShareableTripResult()
class CreateTripError(val error: Throwable?) : ShareableTripResult()

sealed class TripCompletionResult
object TripCompletionSuccess : TripCompletionResult()
class TripCompletionError(val error: Throwable?) : TripCompletionResult()
