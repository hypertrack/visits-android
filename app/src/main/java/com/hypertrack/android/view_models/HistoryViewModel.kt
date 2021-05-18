package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.*
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.utils.Constants
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TimeDistanceFormatter
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val timeDistanceFormatter: TimeDistanceFormatter,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val history = historyRepository.history

    val tiles = MediatorLiveData<List<HistoryTile>>()

    init {
        tiles.addSource(historyRepository.history) {
            if (it.locationTimePoints.isNotEmpty()) {
                Log.d(TAG, "got new history $it")
                val asTiles = historyToTiles(it, timeDistanceFormatter)
                Log.d(TAG, "converted to tiles $asTiles")
                tiles.postValue(asTiles)
            } else {
                Log.d(TAG, "Empty history")
                tiles.postValue(emptyList())
            }
        }
    }

    val error = SingleLiveEvent<String?>()

    fun refreshHistory() {
        viewModelScope.launch {
            when (val res = historyRepository.getHistory()) {
                is HistoryError -> {
                    error.postValue(res.error?.message)
                }
                is History -> {
//                    if (res.locationTimePoints.isEmpty()) {
//                        error.postValue("No history is available.")
//                    }
                }
            }
        }
    }

    private fun historyToTiles(
        history: History,
        timeDistanceFormatter: TimeDistanceFormatter
    ): List<HistoryTile> {
        with(history) {
            val result = mutableListOf<HistoryTile>()
            var startMarker = true
            var ongoingStatus = Status.UNKNOWN
            for (marker in markers.sortedBy { it.timestamp }) {
                when (marker) {
                    is StatusMarker -> {
                        val tile = HistoryTile(
                            marker.status,
                            marker.asDescription(timeDistanceFormatter),
                            if (marker.status == Status.OUTAGE) {
                                mapInactiveReason(marker.reason)
                            } else {
                                marker.address
                            },
                            marker.timeFrame(timeDistanceFormatter),
                            historyTileType(startMarker, marker.status),
                            filterMarkerLocations(
                                marker.startLocationTimestamp ?: marker.startTimestamp,
                                marker.endLocationTimestamp ?: marker.endTimestamp
                                ?: marker.startTimestamp,
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
                "${formatDuration(summary.totalDuration)} • ${
                    timeDistanceFormatter.formatDistance(
                        summary.totalDistance
                    )
                }",
                null, "", HistoryTileType.SUMMARY
            )

            return result.apply { add(0, summaryTile) }
        }
    }

    private fun GeofenceMarker.asDescription(): String {
        //todo string res
        return (metadata["name"]
            ?: metadata["address"]
            ?: metadata
                ).let {
                "$it"
            }
    }

    private fun StatusMarker.asDescription(timeDistanceFormatter: TimeDistanceFormatter): String =
        when (status) {
            Status.DRIVE -> formatDriveStats(timeDistanceFormatter)
            Status.WALK -> formatWalkStats()
            else -> formatDuration(duration)
        }

    private fun StatusMarker.formatDriveStats(timeDistanceFormatter: TimeDistanceFormatter) =
        "${formatDuration(duration)} • ${timeDistanceFormatter.formatDistance(distance ?: 0)}"

    private fun StatusMarker.formatWalkStats() =
        "${formatDuration(duration)}  • ${stepsCount ?: 0} steps"

    private fun formatDuration(duration: Int) = when {
        duration / 3600 < 1 -> "${duration / 60} min"
        duration / 3600 == 1 -> "1 hour ${duration % 3600 / 60} min"
        else -> "${duration / 3600} hours ${duration % 3600 / 60} min"
    }

    private fun StatusMarker.timeFrame(timeFormatter: TimeDistanceFormatter): String {
        if (endTimestamp == null) return timeFormatter.formatTime(startTimestamp)
        return "${timeFormatter.formatTime(startTimestamp)} : ${
            timeFormatter.formatTime(
                endTimestamp
            )
        }"
    }

    private fun GeofenceMarker.asTimeFrame(formatter: TimeDistanceFormatter): String {
        val from = timestamp
        val upTo = exitTimestamp ?: timestamp
        return if (from == upTo)
            formatter.formatTime(timestamp)
        else
            "${formatter.formatTime(from)} : ${formatter.formatTime(upTo)}"
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

    private fun mapInactiveReason(reason: String?): String? {
        return when (reason) {
            "location_permissions_denied" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_location_permissions_denied)
            }
            "location_services_disabled" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_location_services_disabled)
            }
            "motion_activity_permissions_denied" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_motion_activity_permissions_denied)
            }
            "motion_activity_services_disabled" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_motion_activity_services_disabled)
            }
            "motion_activity_services_unavailable" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_motion_activity_services_unavailable)
            }
            "tracking_stopped" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_tracking_stopped)
            }
            "tracking_service_terminated" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_tracking_service_terminated)
            }
            "unexpected" -> {
                osUtilsProvider.getString(R.string.timeline_inactive_reason_unexpected)
            }
            else -> reason
        }
    }

    companion object {
        const val TAG = "HistoryViewModel"
    }
}