package com.hypertrack.android.api

import android.graphics.Bitmap
import com.hypertrack.android.models.Address
import com.hypertrack.android.models.VisitDataSource
import com.hypertrack.android.models.VisitType
import com.hypertrack.android.toBase64
import com.hypertrack.android.toNote
import com.hypertrack.logistics.android.github.R
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @POST("client/devices/{device_id}/start")
    suspend fun clockIn(@Path("device_id") deviceId: String)

    @POST("client/devices/{device_id}/stop")
    suspend fun clockOut(@Path("device_id")deviceId : String)

    @POST("client/devices/{device_id}/image")
    suspend fun persistImage(
        @Path("device_id")deviceId : String,
        @Body encodedImage: EncodedImage
    ) : Response<ImageResponse>

    @GET("client/geofences?include_archived=false&include_markers=true")
    suspend fun getGeofences(
        @Query("device_id")deviceId : String,
        @Query("pagination_token")paginationToken: String
    ) : Response<GeofenceResponse>

    @GET("client/geofences/markers")
    suspend fun getGeofenceMarkers(
        @Query("device_id")deviceId : String,
        @Query("pagination_token")paginationToken: String
    ) : Response<GeofenceMarkersResponse>

    @GET("client/trips")
    suspend fun getTrips(
        @Query("device_id")deviceId : String,
        @Query("pagination_token")paginationToken: String
    ) : Response<TripResponse>

    /**
     * client/devices/A24BA1B4-1234-36F7-8DD7-15D97C3FD912/history/2021-02-05?timezone=Europe%2FZaporozhye
     */
    @GET("client/devices/{device_id}/history/{day}")
    suspend fun getHistory(
        @Path("device_id") deviceId: String,
        @Path("day") day: String,
        @Query("timezone") timezone: String
    ) : Response<History>

}

data class EncodedImage(@field:Json(name = "data") val data: String) {
    constructor(bitmap: Bitmap) : this(bitmap.toBase64())
}

data class TripResponse(
    @field:Json(name = "data") private val _trips: List<Trip>?,
    @field:Json(name = "pagination_token") private val _next: String?
) {
    val trips: List<Trip>
        get() = _trips ?: emptyList()
    val paginationToken: String
        get() = _next ?: ""
}

data class GeofenceMarkersResponse(
    @field:Json(name = "data") private val _markers: List<GeofenceMarker>?,
    @field:Json(name = "pagination_token") val next: String?
) {
    val markers: List<GeofenceMarker>
        get() = _markers ?: emptyList()
}

data class GeofenceResponse(
    @field:Json(name = "data") private val _geofences: List<Geofence>?,
    @field:Json(name = "pagination_token") private val _next: String?
) {
    val geofences: List<Geofence>
        get() = _geofences ?: emptyList()
    val paginationToken: String
        get() = _next ?: ""
}

data class ImageResponse(
    @field:Json(name = "name") val name: String
)

data class Trip(
    @field:Json(name = "views") private val _views: Views?,
    @field:Json(name = "trip_id") val tripId: String?,
    @field:Json(name = "started_at") private val _createdAt: String?,
    @field:Json(name = "metadata") private val _metadata : Map<String, Any>?,
    @field:Json(name = "destination") val destination: TripDestination?
): VisitDataSource {
    override val visitedAt: String
        get() = destination?.arrivedAt?:""
    override val _id: String
        get() = tripId ?: ""
    override val createdAt: String
        get() = _createdAt ?: ""
    override val customerNote: String
        get() = _metadata.toNote()
    override val latitude: Double
        get() = destination?.geometry?.latitude ?: 0.0
    override val longitude: Double
        get() = destination?.geometry?.longitude ?: 0.0
    override val address: Address?
        get() = destination?.address?.let { Address(it, "", "", "") }
    override val visitType: VisitType
        get() = VisitType.TRIP
    override val visitNamePrefixId: Int
        get() = R.string.trip_to
    override val visitNameSuffix: String
        get() = if (destination?.address == null) " [$longitude, $latitude]" else " ${destination.address}"
}

data class TripDestination(
    @field:Json(name = "address") val address: String?,
    @field:Json(name = "geometry") val geometry: Geometry,
    @field:Json(name = "arrived_at") val arrivedAt: String?
)

data class Views(
    @field:Json(name = "share_url") val shareUrl: String?,
    @field:Json(name = "embed_url") val embedUrl: String?
)

data class Geofence(
    @field:Json(name = "geofence_id") val geofence_id : String,
    @field:Json(name = "created_at") val created_at : String,
    @field:Json(name = "metadata") val metadata : Map<String, Any>?,
    @field:Json(name = "geometry") val geometry : Geometry,
    @field:Json(name = "markers") val marker: GeofenceMarkersResponse?,
    @field:Json(name = "radius") val radius : Int
): VisitDataSource {
    override val latitude: Double
        get() = geometry.latitude
    override val longitude: Double
        get() = geometry.longitude
    override val _id: String
        get() = geofence_id
    override val customerNote: String
        get() = metadata.toNote()
    override val address: Address?
        get() = null
    override val createdAt: String
        get() = created_at
    override val visitedAt: String
        get() = marker?.markers?.first()?.arrival?.recordedAt ?: ""
    override val visitType
        get() = VisitType.GEOFENCE
    override val visitNamePrefixId: Int
        get() = R.string.geofence_at
    override val visitNameSuffix: String
        get() = if (address == null) "[$longitude, $latitude]" else "$address"
    val type: String
        get() = geometry.type
}

class Point (
    @field:Json(name = "coordinates") override val coordinates : List<Double>
) : Geometry() {
    override val type: String
        get() = "Point"

    override val latitude: Double
        get() = coordinates[1]

    override val longitude: Double
        get() = coordinates[0]
}

class Polygon (
    @field:Json(name = "coordinates") override val coordinates : List<List<List<Double>>>
) : Geometry() {
    override val type: String
            get() = "Polygon"
    override val latitude: Double
        get() = coordinates[0].map { it[1] }.average()
    override val longitude: Double
        get() = coordinates[0].map { it[0] }.average()
}

abstract class Geometry {
    abstract val coordinates: List<*>
    abstract val type: String
    abstract val latitude: Double
    abstract val longitude: Double
}

data class GeofenceMarker(
    @field:Json(name = "geofence_id") val geofenceId: String,
    @field:Json(name = "arrival") val arrival: Arrival?
)
data class Arrival(@field:Json(name = "recorded_at") val recordedAt: String = "")

data class Insights(
    val active_duration: Int,
    val drive_distance: Int,
    val drive_duration: Int,
    val estimated_distance: Int,
    val geofences_count: Int,
    val geofences_idle_time: Int,
    val geofences_route_to_time: Int,
    val geofences_time: Int,
    val geotags_count: Int,
    val geotags_route_to_time: Int,
    val inactive_duration: Int,
    val inactive_reasons: List<Any>,
    val step_count: Int,
    val stop_duration: Int,
    val total_tracking_time: Int,
    val tracking_rate: Int,
    val trips_arrived_at_destination: Int,
    val trips_count: Int,
    val trips_on_time: Int,
    val walk_duration: Int
)

data class History(
    val totalDistance: Int,
    val insights: Insights,
) : HistoryResult()

class HistoryError(val error: Throwable?) : HistoryResult()
sealed class HistoryResult