package com.hypertrack.android.models

data class History(
    val summary: Summary,
    val locationTimePoints: List<Pair<Location, Long>>,
    val markers: List<Marker>,
)

data class Summary(
    val totalDistance: Int,
    val totalDuration: Int,
    val totalDriveDistance: Int,
    val totalDriveDuration: Int,
)

data class Location(
    val longitude: Double,
    val latitude: Double
)

data class Marker(
    val type: MarkerType,
    val timestamp: Long,
    val location: Location,
)

enum class MarkerType{
    STATUS,
    GEOTAG,
    GEOFENCE_ENTRY
}