package com.hypertrack.android.api

import com.google.gson.annotations.SerializedName
import com.hypertrack.android.models.Address
import com.hypertrack.android.models.VisitDataSource
import com.hypertrack.android.models.VisitType
import com.hypertrack.android.toNote
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("client/devices/{device_id}/start")
    suspend fun clockIn(@Path("device_id") deviceId: String)

    @POST("client/devices/{device_id}/stop")
    suspend fun clockOut(@Path("device_id")deviceId : String)

    @GET("client/devices/{device_id}/geofences")
    suspend fun getGeofences(@Path("device_id")deviceId : String) : List<Geofence>

    @GET("client/trips")
    suspend fun getTrips(
        @Query("device_id")deviceId : String,
        @Query("pagination_token")paginationToken: String = ""
    ) : TripResponse

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

data class Trip(
    @SerializedName("views") private val _views: Views?,
    @SerializedName("trip_id") val tripId: String?,
    @SerializedName("started_at") private val _createdAt: String?,
    @SerializedName("metadata") private val _metadata : Map<String, Any>?,
    @SerializedName("destination") val destination: TripDestination?
): VisitDataSource {
    val shareUrl: String
        get() = _views?.shareUrl ?: ""
    override val visitId: String
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
}

data class TripDestination(
    @SerializedName("address") val address: String?,
    @SerializedName("geometry") val geometry: Geometry
)

data class Views(@SerializedName("share_url") val shareUrl: String?)

data class Geofence(
    @SerializedName("all_devices") val all_devices : Boolean?,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("delete_at") val delete_at : String?,
    @SerializedName("device_id") val device_id : String,
    @SerializedName("device_ids") val device_ids : List<String>?,
    @SerializedName("geofence_id") val geofence_id : String,
    @SerializedName("geometry") val geometry : Geometry,
    @SerializedName("metadata") val metadata : Map<String, Any>?,
    @SerializedName("radius") val radius : Int,
    @SerializedName("single_use") val single_use : Boolean
): VisitDataSource {
    override val latitude: Double
        get() = geometry.latitude
    override val longitude: Double
        get() = geometry.longitude
    override val visitId: String
        get() = geofence_id
    override val customerNote: String
        get() = metadata.toNote()
    override val address: Address?
        get() = null
    override val createdAt: String
        get() = created_at
    override val visitType
        get() = VisitType.GEOFENCE
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
    @SerializedName("coordinates") override val coordinates : List<List<Double>>
) : Geometry() {
    override val type: String
            get() = "Polygon"
    override val latitude: Double
        get() = coordinates.map { it[1] }.average()
    override val longitude: Double
        get() = coordinates.map { it[0] }.average()
}

abstract class Geometry {
    abstract val coordinates: List<*>
    abstract val type: String
    abstract val latitude: Double
    abstract val longitude: Double
}

