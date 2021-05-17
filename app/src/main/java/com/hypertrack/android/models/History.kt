package com.hypertrack.android.models

import android.util.Log
import com.hypertrack.android.utils.Constants
import com.hypertrack.android.utils.TimeDistanceFormatter
import com.squareup.moshi.JsonClass

data class History(
    val summary: Summary,
    val locationTimePoints: List<Pair<Location, String>>,
    val markers: List<Marker>,
) : HistoryResult()

val EMPTY_HISTORY: History = History(
    Summary(0, 0, 0, 0, 0, 0, 0),
    emptyList(),
    emptyList()
)

data class Summary(
    val totalDistance: Int,
    val totalDuration: Int,
    val totalDriveDistance: Int,
    val totalDriveDuration: Int,
    val stepsCount: Int,
    val totalWalkDuration: Int,
    val totalStopDuration: Int,
)

@JsonClass(generateAdapter = true)
data class Location(
    val longitude: Double,
    val latitude: Double
)

interface Marker {
    val type: MarkerType
    val timestamp: String
    val location: Location?
}

data class StatusMarker(
    override val type: MarkerType,
    override val timestamp: String,
    override val location: Location?,
    val startLocation: Location?,
    val endLocation: Location?,
    val startTimestamp: String,
    val endTimestamp: String?,
    val startLocationTimestamp: String?,
    val endLocationTimestamp: String?,
    val status: Status,
    val duration: Int,
    val distance: Int?,
    val stepsCount: Int?,
    val address: String?,
    val reason: String?,
) : Marker

data class GeofenceMarker(
    override val type: MarkerType,
    override val timestamp: String,
    override val location: Location?,
    val metadata: Map<String, Any>,
    val arrivalLocation: Location?,
    val exitLocation: Location?,
    val arrivalTimestamp: String?,
    val exitTimestamp: String?,
) : Marker

data class GeoTagMarker(
    override val type: MarkerType = MarkerType.GEOTAG,
    override val timestamp: String,
    override val location: Location?,
    val metadata: Map<String, Any>,
) : Marker

enum class MarkerType {
    STATUS,
    GEOTAG,
    GEOFENCE_ENTRY
}

enum class Status {
    OUTAGE,
    INACTIVE,
    DRIVE,
    WALK,
    STOP,
    UNKNOWN
}

class HistoryError(val error: Throwable?) : HistoryResult()
sealed class HistoryResult


data class HistoryTile(
    val status: Status,
    val description: CharSequence,
    val address: CharSequence?,
    val timeframe: String,
    val tileType: HistoryTileType,
    val locations: List<Location> = emptyList(),
    val isStatusTile: Boolean = true,
)

enum class HistoryTileType {
    OUTAGE_START,
    OUTAGE,
    ACTIVE_START,
    ACTIVE,
    SUMMARY
}

fun List<HistoryTile>.asHistory() = History(
    Summary(0, 0, 0, 0, 0, 0, 0),
    flatMap { it.locations }.map { it to "2020-02-02T20:20:02.020Z" },
    emptyList()
)

private const val TAG = "History"