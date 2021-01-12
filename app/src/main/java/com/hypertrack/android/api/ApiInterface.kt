package com.hypertrack.android.api

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.hypertrack.android.models.Address
import com.hypertrack.android.models.VisitDataSource
import com.hypertrack.android.models.VisitType
import com.hypertrack.android.toBase64
import com.hypertrack.android.toNote
import com.hypertrack.logistics.android.github.R
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

}

data class EncodedImage(@SerializedName("data") val data: String) {
    constructor(bitmap: Bitmap) : this(bitmap.toBase64())
}

data class TripResponse(
    @SerializedName("data") private val _trips: List<Trip>?,
    @SerializedName("pagination_token") private val _next: String?
) {
    val trips: List<Trip>
        get() = _trips ?: emptyList()
    val paginationToken: String
        get() = _next ?: ""
}

data class GeofenceMarkersResponse(
    @SerializedName("data") private val _markers: List<GeofenceMarker>?,
    @SerializedName("pagination_token") val next: String?
) {
    val markers: List<GeofenceMarker>
        get() = _markers ?: emptyList()
}

data class GeofenceResponse(
    @SerializedName("data") private val _geofences: List<Geofence>?,
    @SerializedName("pagination_token") private val _next: String?
) {
    val geofences: List<Geofence>
        get() = _geofences ?: emptyList()
    val paginationToken: String
        get() = _next ?: ""
}

data class ImageResponse(
    @SerializedName("name") val name: String
)

data class Trip(
    @SerializedName("views") private val _views: Views?,
    @SerializedName("trip_id") val tripId: String?,
    @SerializedName("started_at") private val _createdAt: String?,
    @SerializedName("metadata") private val _metadata : Map<String, Any>?,
    @SerializedName("destination") val destination: TripDestination?
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
    @SerializedName("address") val address: String?,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("arrived_at") val arrivedAt: String?
)

data class Views(
    @SerializedName("share_url") val shareUrl: String?,
    @SerializedName("embed_url") val embedUrl: String?
)

data class Geofence(
    @SerializedName("geofence_id") val geofence_id : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("metadata") val metadata : Map<String, Any>?,
    @SerializedName("geometry") val geometry : Geometry,
    @SerializedName("markers") val marker: GeofenceMarkersResponse?,
    @SerializedName("radius") val radius : Int
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
    @SerializedName("coordinates") override val coordinates : List<Double>
) : Geometry() {
    override val type: String
        get() = "Point"

    override val latitude: Double
        get() = coordinates[1]

    override val longitude: Double
        get() = coordinates[0]
}

class Polygon (
    @SerializedName("coordinates") override val coordinates : List<List<List<Double>>>
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
    @SerializedName("geofence_id") val geofenceId: String,
    @SerializedName("arrival") val arrival: Arrival?
)
data class Arrival(@SerializedName("recorded_at") val recordedAt: String = "")
