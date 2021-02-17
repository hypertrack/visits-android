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
        /** Always 0 as removed from API response */
        val stepsCount: Int = 0,
        val totalWalkDuration: Int,
        val totalStopDuration: Int,
)

data class Location(
    val longitude: Double,
    val latitude: Double
)

data class Marker(
    val type: MarkerType,
    val timestamp: String,
    val location: Location?,
)

enum class MarkerType{
    STATUS,
    GEOTAG,
    GEOFENCE_ENTRY
}


class HistoryError(val error: Throwable?) : HistoryResult()
sealed class HistoryResult