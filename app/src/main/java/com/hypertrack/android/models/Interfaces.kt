package com.hypertrack.android.models

import com.hypertrack.android.api.TripParams


interface AbstractBackendProvider : TripManagementApi

interface TripManagementApi {
    suspend fun createTrip(tripParams: TripParams) : ShareableTripResult
    suspend fun completeTrip(tripId: String) : TripCompletionResult
}

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


interface HomeManagementApi {
    suspend fun getHomeLocation(): HomeLocationResult
    suspend fun updateHomeLocation(homeLocation: GeofenceLocation): HomeUpdateResult
}

sealed class HomeLocationResult
object NoHomeLocation : HomeLocationResult()
data class GeofenceLocation(val latitude: Double, val longitude: Double) : HomeLocationResult()
class HomeLocationResultError(val error: Throwable?) : HomeLocationResult()

sealed class HomeUpdateResult
object HomeUpdateResultSuccess : HomeUpdateResult()
class HomeUpdateResultError(val error: Throwable?) : HomeUpdateResult()
