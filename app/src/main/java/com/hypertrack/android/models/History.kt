package com.hypertrack.android.models

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
) {
    companion object {
        private val ashburyXHeight = Location(-122.4470, 37.7700)
        private val ashburyXHeightLoitering = listOf(
            ashburyXHeight,
            Location(-122.4469, 37.7701),
            Location(-122.4470, 37.7700),
            Location(-122.4471, 37.7700),
            Location(-122.4470, 37.7699),
            ashburyXHeight

        )
        private val botanicGarden = Location(-122.4546, 37.7718)
        private val ashburyXHeight2BotanicGarden = listOf(
            ashburyXHeight,
            Location(-122.4471, 37.7705),
            Location(-122.4471, 37.7709),
            Location(-122.4472, 37.7714),
            Location(-122.4473, 37.7719),
            Location(-122.4464, 37.7720),
            Location(-122.4464, 37.7720),
            Location(-122.4458, 37.7721),
            Location(-122.4458, 37.7726),
            Location(-122.4459, 37.7730),
            Location(-122.4468, 37.7729),
            Location(-122.4480, 37.7727),
            Location(-122.4495, 37.7725),
            Location(-122.4510, 37.7723),
            Location(-122.4524, 37.7722),
            Location(-122.4538, 37.7716),
            Location(-122.4538, 37.7716),
            botanicGarden,
        )
        private val BotanicGarden2AshburyXHeight = listOf(
            botanicGarden,
            Location(-122.4550, 37.7712),
            Location(-122.4540, 37.7714),
            Location(-122.4526, 37.7713),
            Location(-122.4508, 37.7714),
            Location(-122.4505, 37.7707),
            Location(-122.4503, 37.7699),
            Location(-122.4500, 37.7696),
            Location(-122.4494, 37.7697),
            Location(-122.4488, 37.7698),
            Location(-122.4481, 37.7699),
            Location(-122.4475, 37.7699),
            Location(-122.4470, 37.7700),
            ashburyXHeight,
        )
        private val botanicGardenLoitering = listOf(
            botanicGarden,
            Location(-122.4544, 37.7718),
            Location(-122.4547, 37.7719),
            Location(-122.4548, 37.7721),
            Location(-122.4550, 37.7719),
            Location(-122.4549, 37.7718),
            botanicGarden

        )

        val MOCK_TILES = listOf(
            HistoryTile(
                Status.STOP,"1hr 50 min • 3.2 miles",null,
                "10:17 am",
                HistoryTileType.SUMMARY,
                ashburyXHeightLoitering,
            ),
            HistoryTile(
                Status.STOP,"5 min","1906 Mission, San Francisco, CA",
                "10:17 am-10:22 am",
                HistoryTileType.ACTIVE_START,
                ashburyXHeightLoitering,
            ),
            HistoryTile(
                Status.WALK,
                "10 min • 520 steps",
                null,
                "10:22 am-10:32 am",
                HistoryTileType.ACTIVE,
                ashburyXHeight2BotanicGarden
            ),
            HistoryTile(
                Status.STOP,
                "30 min",
                "McLaren Lodge, Fell, San Francisco, CA",
                "10:32 am-11:02 am",
                HistoryTileType.ACTIVE,
                botanicGardenLoitering
            ),
            HistoryTile(
                Status.DRIVE,
                "5 min • 3.2 miles",
                null,
                "11:02 am-11:07 am",
                HistoryTileType.ACTIVE,
                BotanicGarden2AshburyXHeight
            ),
            HistoryTile(
                Status.OUTAGE,
                "Location services disabled",
                null,
                "11:07 am-12:07 pm",
                HistoryTileType.OUTAGE,
                ashburyXHeightLoitering
            ),
        )
    }

}

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
    }

    return result
}

private fun filterMarkerLocations(
    marker: StatusMarker,
    locationTimePoints: List<Pair<Location, String>>
): List<Location> {
    val from = marker.startLocationTimestamp ?: return emptyList()
    val upTo = marker.endLocationTimestamp ?: return emptyList()

    return locationTimePoints
        .filter { (_, time) -> time in upTo..from }
        .map { (loc, _) -> loc }
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
    "${formatDuration()} • ${distance ?:0 / 1000} km"

private fun StatusMarker.formatWalkStats() =
    "${formatDuration()}  • ${stepsCount?:0} steps"


private fun StatusMarker.timeFrame(): String {
    if (endTimestamp == null) return "XXam:XX"
    return "XXam:XX YYpm:YY"
}