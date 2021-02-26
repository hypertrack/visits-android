package com.hypertrack.android.models

import androidx.annotation.DrawableRes
import com.hypertrack.logistics.android.github.R

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

data class Marker(
        val type: MarkerType,
        val timestamp: String,
        val location: Location?,
)

enum class MarkerType {
    STATUS,
    GEOTAG,
    GEOFENCE_ENTRY
}


class HistoryError(val error: Throwable?) : HistoryResult()
sealed class HistoryResult
data class HistoryTile(
    @DrawableRes val icon: Int,
    val description: CharSequence,
    val address: CharSequence?,
    val id: Int,
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
                R.drawable.ic_coffee, "5 min", "1906 Mission, San Francisco, CA", 0,
                ashburyXHeightLoitering
            ),
            HistoryTile(
                R.drawable.ic_walk,
                "10 min • 520 steps",
                null,
                1,
                ashburyXHeight2BotanicGarden
            ),
            HistoryTile(
                R.drawable.ic_coffee,
                "30 min",
                "McLaren Lodge, Fell, San Francisco, CA",
                2,
                botanicGardenLoitering
            ),
            HistoryTile(
                R.drawable.ic_car,
                "5 min • 3.2 miles",
                null,
                1,
                BotanicGarden2AshburyXHeight
            ),
        )
    }

}

fun List<HistoryTile>.asHistory() = History(
    Summary(0, 0, 0, 0, 0, 0, 0),
    flatMap { it.locations }.map { it to "2020-02-02T20:20:02.020Z" },
    emptyList()
)