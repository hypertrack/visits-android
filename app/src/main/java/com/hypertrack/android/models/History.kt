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

fun History.asTiles(timeDistanceFormatter: TimeDistanceFormatter): List<HistoryTile> {
    val result = mutableListOf<HistoryTile>()
    var startMarker = true
    var ongoingStatus = Status.UNKNOWN
    for (marker in markers.sortedBy { it.timestamp }) {
        when (marker) {
            is StatusMarker -> {
                val tile = HistoryTile(
                    marker.status,
                    marker.asDescription(timeDistanceFormatter),
                    marker.address,
                    marker.timeFrame(timeDistanceFormatter),
                    historyTileType(startMarker, marker.status),
                    filterMarkerLocations(
                        marker.startLocationTimestamp ?: marker.startTimestamp,
                        marker.endLocationTimestamp ?: marker.endTimestamp ?: marker.startTimestamp,
                        locationTimePoints
                    )
                )
                ongoingStatus = marker.status
                result.add(tile)
            }
            is GeoTagMarker -> {
                marker.location?.let { geotagLocation ->
                    val tile = HistoryTile(
                        ongoingStatus,
                        marker.asDescription(), null,
                        timeDistanceFormatter.formatTime(marker.timestamp),
                        historyTileType(startMarker, ongoingStatus),
                        listOf(geotagLocation), false
                    )
                    result.add(tile)
                }
            }
            is GeofenceMarker -> {
                val tile = HistoryTile(
                    ongoingStatus,
                    marker.asDescription(), null,
                    marker.asTimeFrame(timeDistanceFormatter),
                    historyTileType(startMarker, ongoingStatus),
                    filterMarkerLocations(
                        marker.arrivalTimestamp ?: marker.timestamp,
                        marker.exitTimestamp ?: marker.timestamp,
                        locationTimePoints
                    ), false
                )
                result.add(tile)
            }
        }
        startMarker = result.isEmpty()
    }

    val summaryTile = HistoryTile(
        Status.UNKNOWN,
        "${formatDuration(summary.totalDuration)} • ${timeDistanceFormatter.formatDistance(summary.totalDistance)}",
        null, "", HistoryTileType.SUMMARY
    )

    return result.apply { add(0, summaryTile) }
}

private fun historyTileType(
    startMarker: Boolean,
    status: Status
): HistoryTileType {
    return when {
        startMarker && status in listOf(
            Status.OUTAGE,
            Status.INACTIVE
        ) -> HistoryTileType.OUTAGE_START
        startMarker -> HistoryTileType.ACTIVE_START
        status in listOf(Status.OUTAGE, Status.INACTIVE) -> HistoryTileType.OUTAGE
        else -> HistoryTileType.ACTIVE
    }
}

//todo string res
private fun GeoTagMarker.asDescription(): String = when {
    metadata.containsValue(Constants.CLOCK_IN) -> "Clock In"
    metadata.containsValue(Constants.CLOCK_OUT) -> "Clock Out"
    metadata.containsValue(Constants.PICK_UP) -> "Pick Up"
    metadata.containsValue(Constants.VISIT_MARKED_CANCELED) -> "Visit Marked Cancelled"
    metadata.containsValue(Constants.VISIT_MARKED_COMPLETE) -> "Visit Marked Complete"
    else -> "Geotag $metadata"
}

private fun GeofenceMarker.asDescription(): String {
    //todo string res
    return (metadata["name"]
        ?: metadata["address"]
        ?: metadata
            ).let {
            "Geofence $it"
        }
}

private fun StatusMarker.asDescription(timeDistanceFormatter: TimeDistanceFormatter): String =
    when (status) {
        Status.DRIVE -> formatDriveStats(timeDistanceFormatter)
        Status.WALK -> formatWalkStats()
        else -> formatDuration(duration)
    }

private fun filterMarkerLocations(
    from: String,
    upTo: String,
    locationTimePoints: List<Pair<Location, String>>
): List<Location> {

    check(locationTimePoints.isNotEmpty()) { "locations should not be empty for the timeline" }
    val innerLocations = locationTimePoints
        .filter { (_, time) -> time in from..upTo }
        .map { (loc, _) -> loc }
    if (innerLocations.isNotEmpty()) return innerLocations

    // Snap to adjacent
    val sorted = locationTimePoints.sortedBy { it.second }
    Log.v(TAG, "Got sorted $sorted")
    val startLocation = sorted.lastOrNull { (_, time) -> time < from }
    val endLocation = sorted.firstOrNull { (_, time) -> time > upTo }
    Log.v(TAG, "Got start $startLocation, end $endLocation")
    return listOfNotNull(startLocation?.first, endLocation?.first)

}


private fun formatDuration(duration: Int) = when {
    duration / 3600 < 1 -> "${duration / 60} min"
    duration / 3600 == 1 -> "1 hour ${duration % 3600 / 60} min"
    else -> "${duration / 3600} hours ${duration % 3600 / 60} min"
}

private fun StatusMarker.formatDriveStats(timeDistanceFormatter: TimeDistanceFormatter) =
    "${formatDuration(duration)} • ${timeDistanceFormatter.formatDistance(distance ?: 0)}"

private fun StatusMarker.formatWalkStats() =
    "${formatDuration(duration)}  • ${stepsCount ?: 0} steps"


private fun StatusMarker.timeFrame(timeFormatter: TimeDistanceFormatter): String {
    if (endTimestamp == null) return timeFormatter.formatTime(startTimestamp)
    return "${timeFormatter.formatTime(startTimestamp)} : ${timeFormatter.formatTime(endTimestamp)}"
}

private fun GeofenceMarker.asTimeFrame(formatter: TimeDistanceFormatter): String {
    val from = timestamp
    val upTo = exitTimestamp ?: timestamp
    return if (from == upTo)
        formatter.formatTime(timestamp)
    else
        "${formatter.formatTime(from)} : ${formatter.formatTime(upTo)}"
}

private const val TAG = "History"