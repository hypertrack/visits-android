package com.hypertrack.android.models

import android.util.Log

data class History(
        val summary: Summary,
        val locationTimePoints: List<Pair<Location, String>>,
        val markers: List<Marker>,
) : HistoryResult()

data class Summary(
        val totalDistance: Int,
        val totalDuration: Int,
        val totalDriveDistance: Int,
        val totalDriveDuration: Int,
        val stepsCount: Int,
        val totalWalkDuration: Int,
        val totalStopDuration: Int,
)

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
) : Marker

data class GenericMarker(
    override val type: MarkerType,
    override val timestamp: String,
    override val location: Location?,
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
    val locations: List<Location> = emptyList()
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

fun History.asTiles(): List<HistoryTile> {
    val result = mutableListOf<HistoryTile>()
    val statusMarkers = markers.filterIsInstance<StatusMarker>().sortedBy {
        it.startTimestamp
    }
    var startMarker = true
    for (marker in statusMarkers) {
        val tile = HistoryTile(
            marker.status,
            marker.asDescription(),
            marker.address,
            marker.timeFrame(),
            when {
                startMarker && marker.status in listOf(Status.OUTAGE, Status.INACTIVE) -> {
                    startMarker = false; HistoryTileType.OUTAGE_START
                }
                startMarker -> {
                    startMarker = false; HistoryTileType.ACTIVE_START
                }
                marker.status in listOf(Status.OUTAGE, Status.INACTIVE) -> HistoryTileType.OUTAGE
                else -> HistoryTileType.ACTIVE
            },
            filterMarkerLocations(marker, locationTimePoints)
        )
        result.add(tile)
    }

    return result
}

private fun filterMarkerLocations(
    marker: StatusMarker,
    locationTimePoints: List<Pair<Location, String>>
): List<Location> {
    val from = marker.startLocationTimestamp ?: marker.startTimestamp
    val upTo = marker.endLocationTimestamp ?: marker.endTimestamp ?: marker.startTimestamp

    Log.v(TAG, "filterMarkerLocations from $from to $upTo for marker $marker")
    check(locationTimePoints.isNotEmpty()) { "locations should not be empty for the timeline" }
    val innerLocations = locationTimePoints
        .filter { (_, time) -> time in from..upTo }
        .map { (loc, _) -> loc }
    if (innerLocations.isNotEmpty()) return innerLocations

    // Snap to adjacent
    val sorted =  locationTimePoints.sortedBy { it.second }
    Log.v(TAG, "Got sorted $sorted")
    val startLocation = sorted.lastOrNull { (_, time) -> time < from }
    val endLocation = sorted.firstOrNull { (_, time) -> time > upTo }
    Log.v(TAG, "Got start $startLocation, end $endLocation")
    return listOfNotNull(startLocation?.first, endLocation?.first)

}

private fun StatusMarker.asDescription(): String = when(status) {
        Status.DRIVE -> formatDriveStats()
        Status.WALK -> formatWalkStats()
        else -> formatDuration()
    }

private fun StatusMarker.formatDuration() = when {
    duration / 3600 < 1 -> "${duration / 60} min"
    duration / 3600 == 1 -> "1 hour ${duration % 3600 / 60} min"
    else -> "${duration / 3600} hours ${duration % 3600 / 60} min"
}
private fun StatusMarker.formatDriveStats() =
    "${formatDuration()} • ${(distance ?:0) / 1000} km"

private fun StatusMarker.formatWalkStats() =
    "${formatDuration()}  • ${stepsCount?:0} steps"


private fun StatusMarker.timeFrame(): String {
    if (endTimestamp == null) return "XXam:XX"
    return "XXam:XX YYpm:YY"
}

private const val TAG = "History"