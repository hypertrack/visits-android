package com.hypertrack.android.api

import android.graphics.Bitmap
import android.util.Log
import com.hypertrack.android.models.*
import com.hypertrack.android.models.GeofenceMarker
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.utils.Injector
import com.hypertrack.logistics.android.github.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ApiClient(
        accessTokenRepository: AccessTokenRepository,
        baseUrl: String,
        private val deviceId: String
) : AbstractBackendProvider {

    @Suppress("unused")
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    val api: ApiInterface = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(Injector.getMoshi()))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(OkHttpClient.Builder()
                    .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                    .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
                    .addInterceptor(UserAgentInterceptor())
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS).apply {
                        if (BuildConfig.DEBUG) {
                            addInterceptor(loggingInterceptor)
                        }
                    }.build())
            .build()
            .create(ApiInterface::class.java)

    suspend fun clockIn() = api.clockIn(deviceId)

    suspend fun clockOut() = api.clockOut(deviceId)

    suspend fun getGeofences(): List<Geofence> {
        val res = mutableListOf<Geofence>()
        var paginationToken: String? = null
        try {
            do {
                val response = api.getGeofences(deviceId, paginationToken ?: "null")
                if (response.isSuccessful) {
                    response.body()?.geofences?.let {
                        res.addAll(it)
                    }
                    paginationToken = response.body()?.paginationToken
                } else {
                    return emptyList()
                }
                //todo handle error
            } while (paginationToken != null)
            return res
        } catch (e: Exception) {
            Log.e(TAG, "Got exception while fetching geofences $e")
            return listOf()
        }
    }

    suspend fun createGeofence(
        latitude: Double,
        longitude: Double,
        metadata: Map<String, String>
    ): Response<List<Geofence>> {
        return api.createGeofences(
            deviceId,
            GeofenceParams(
                setOf(
                    GeofenceProperties(
                        Point(listOf(longitude, latitude)),
                        metadata, 100
                    )
                ), deviceId
            )
        )
    }

    suspend fun getTrips(page: String = ""): List<Trip> {
        try {
            val response = api.getTrips(deviceId, page)
            if (response.isSuccessful) {
                // Log.v(TAG, "Got response ${response.body()}")
                return response.body()?.trips?.filterNot {
                    it.destination == null || it.tripId.isEmpty()
                }
                    ?: emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Got exception while trying to refresh trips $e")
            throw e
        }
        return emptyList()
    }

    suspend fun uploadImage(filename: String, image: Bitmap) {
        try {
            val response = api.persistImage(deviceId, EncodedImage(filename, image))
            if (response.isSuccessful) {
                // Log.v(TAG, "Got post image response ${response.body()}")
            } else {
                throw HttpException(response)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Got exception $e uploading image")
            throw e
        }
    }

    suspend fun getHistory(day: LocalDate, timezone: ZoneId): HistoryResult {
        try {
            with(api.getHistory(deviceId, day.format(DateTimeFormatter.ISO_LOCAL_DATE), timezone.id)) {
                if (isSuccessful) {
                    return body().asHistory()
                } else {
                    return HistoryError(HttpException(this))
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Got exception $e fetching device history")
            return HistoryError(e)
        }

    }

    override suspend fun createTrip(tripParams: TripParams): ShareableTripResult {
        return try {
            with(api.createTrip(tripParams)) {
                if (isSuccessful) {
                    val trip = body()!!
                    ShareableTripSuccess(trip.views.shareUrl, trip.views.embedUrl, trip.tripId, trip.estimate?.route?.remainingDuration )
                }
                else CreateTripError(HttpException(this))
            }
        } catch (t: Throwable) {
             CreateTripError(t)
        }
    }

    override suspend fun completeTrip(tripId: String): TripCompletionResult {
        return try {
            with (api.completeTrip(tripId)) {
                if (isSuccessful) TripCompletionSuccess
                else TripCompletionError(HttpException(this))
            }
        } catch (t: Throwable) {
            TripCompletionError(t)
        }
    }

    override fun getHomeGeofenceLocation(resultHandler: ResultHandler<GeofenceLocation?>) {
        TODO("Not yet implemented")
    }

    override fun updateHomeGeofence(
        homeLocation: GeofenceLocation,
        resultHandler: ResultHandler<Void?>
    ) {
        TODO("Not yet implemented")
    }

    companion object {
        const val TAG = "ApiClient"
    }

}

private fun HistoryResponse?.asHistory(): HistoryResult {
    return if (this == null) {
        HistoryError(null)
    } else {
        History(
                Summary(
                        distance,
                        duration,
                        distance,
                        driveDuration ?: 0,
                        stepsCount ?: 0,
                        walkDuration,
                        stopDuration,
                ),
                locations.coordinates.map { Location(it.longitude, it.latitude) to it.timestamp },
                markers.map { it.asMarker() }
        )
    }
}

private fun HistoryMarker.asMarker(): Marker {
    return when (this) {
        is HistoryStatusMarker -> asStatusMarker()
        is HistoryTripMarker ->
            GeoTagMarker(MarkerType.GEOTAG, data.recordedAt, data.location?.asLocation(), data.metadata?: emptyMap())
        is HistoryGeofenceMarker -> asGeofenceMarker()
        else -> throw IllegalArgumentException("Unknown marker type $type")
    }
}

private fun HistoryGeofenceMarker.asGeofenceMarker(): Marker {
    return GeofenceMarker(
        MarkerType.GEOFENCE_ENTRY,
        data.arrival.location.recordedAt,
        data.arrival.location.geometry?.asLocation(),
        data.geofence.metadata?: emptyMap(),
        data.arrival.location.geometry?.asLocation(),
        data.exit?.location?.geometry?.asLocation(),
        data.arrival.location.recordedAt,
        data.exit?.location?.recordedAt
    )
}


private fun HistoryTripMarkerLocation.asLocation() = Location(coordinates[0], coordinates[1])
private fun Geometry.asLocation() = Location(longitude, latitude)

private fun HistoryStatusMarker.asStatusMarker() = StatusMarker(
    MarkerType.STATUS,
    data.start.recordedAt,
    data.start.location?.geometry?.asLocation(),
    data.start.location?.geometry?.asLocation(),
    data.end.location?.geometry?.asLocation(),
    data.start.recordedAt,
    data.end.recordedAt,
    data.start.location?.recordedAt,
    data.end.location?.recordedAt,
    when (data.value) {
        "inactive" -> Status.INACTIVE
        "active" -> when (data.activity) {
            "stop" -> Status.STOP
            "drive" -> Status.DRIVE
            "walk" -> Status.WALK
            else -> Status.UNKNOWN
        }
        else -> Status.UNKNOWN

    },
    data.duration,
    data.distance,
    data.steps,
    data.address
)