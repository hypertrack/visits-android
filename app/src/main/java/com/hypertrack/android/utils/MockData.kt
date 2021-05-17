package com.hypertrack.android.utils

import com.hypertrack.android.api.HistoryResponse
import com.hypertrack.android.api.asMarker
import com.hypertrack.android.models.*
import com.hypertrack.android.ui.common.toAddressString
import com.hypertrack.logistics.android.github.R
import retrofit2.Response
import java.io.BufferedReader
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object MockData {

    val addr = listOf(
        "2875 El Camino Real",
        "567 Melville Ave",
        "1295 Middlefield Rd",
        "630 Seale Ave",
        "1310 Bryant St",
        "475 Homer Ave",
        "1102 Ramona St",
        "117 University Ave",
        "130 Lytton Ave",
    )

    const val MOCK_GEOFENCES_JSON =
        "{\"data\":[{\"geofence_id\":\"79eb01c2-1f86-4fc6-9200-fa10b7221455\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"single_use\":false,\"created_at\":\"2021-05-12T14:05:11.555811+00:00\",\"metadata\":{\"name\":\"Warehouse\",\"address\":\"Mountain View,  Showers Drive, 190\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.10794627666472,37.40846911725437]},\"archived\":false,\"geofence_type\":\"device\",\"radius\":100},{\"geofence_id\":\"063f1527-9753-468c-827f-55e7830a13db\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"single_use\":false,\"created_at\":\"2021-05-12T13:27:49.301081+00:00\",\"metadata\":{\"name\":null,\"address\":\"Palo Alto,  Alma Street, 2879\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.13376723229887,37.42496597636671]},\"archived\":false,\"geofence_type\":\"device\",\"radius\":100,\"markers\":{\"data\":[{\"marker_id\":\"cee17656-9ba0-4fe0-9064-066f90bda786\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"trip_id\":null,\"created_at\":\"2021-05-12T14:03:15.341Z\",\"arrival\":{\"recorded_at\":\"2021-05-12T13:23:15.341Z\",\"location\":{\"coordinates\":[-122.134206,37.424741,-6.33],\"type\":\"Point\"}},\"exit\":{\"recorded_at\":\"2021-05-12T14:05:35.972Z\",\"location\":{\"coordinates\":[-122.134227,37.424749,3.22],\"type\":\"Point\"}},\"outage_event\":null,\"geofence_type\":\"device\",\"geofence_id\":\"063f1527-9753-468c-827f-55e7830a13db\",\"metadata\":{\"name\":null,\"address\":\"Palo Alto,  Alma Street, 2879\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.13376723229887,37.42496597636671]},\"route_to\":{\"duration\":10000,\"distance\":10000,\"idle_time\":0,\"started_at\":\"2021-05-12T14:03:14.423Z\"},\"radius\":100,\"duration\":2520},{\"marker_id\":\"ff6334eb-60d3-435a-849a-3541f660c471\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"trip_id\":null,\"created_at\":\"2021-05-12T13:57:13.684Z\",\"arrival\":{\"recorded_at\":\"2021-05-11T11:57:13.684Z\",\"location\":{\"coordinates\":[-122.134201,37.424739,3.77],\"type\":\"Point\"}},\"exit\":{\"recorded_at\":\"2021-05-11T12:00:00.278Z\",\"location\":null},\"outage_event\":null,\"geofence_type\":\"device\",\"geofence_id\":\"063f1527-9753-468c-827f-55e7830a13db\",\"metadata\":{\"name\":null,\"address\":\"Palo Alto,  Alma Street, 2879\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.13376723229887,37.42496597636671]},\"route_to\":{\"duration\":5200,\"distance\":4300,\"idle_time\":0,\"started_at\":\"2021-05-12T13:57:12.897Z\"},\"radius\":100,\"duration\":180}],\"pagination_token\":null,\"links\":{\"next\":null}}}],\"pagination_token\":null,\"links\":{\"next\":null}}"
    private val MOCK_HISTORY_JSON by lazy {
        MyApplication.context.resources.openRawResource(R.raw.mock_history).bufferedReader()
            .use { it.readText() }
    }

    val MOCK_SUMMARY = Summary(
        totalDistance = 12 * 857,
        totalDuration = 3 * 57 * 58,
        totalDriveDistance = 4 * 997,
        totalDriveDuration = 1 * 59 * 57,
        stepsCount = 3794,
        totalWalkDuration = 2 * 57 * 59,
        totalStopDuration = 20 * 57,
    )

    val MOCK_HISTORY: HistoryResult = Response.success(
        Injector.getMoshi().adapter(HistoryResponse::class.java)
            .fromJson(MOCK_HISTORY_JSON)
    ).body()!!.let { h ->
        History(
            MOCK_SUMMARY,
            h.locations.coordinates.map { Location(it.longitude, it.latitude) to it.timestamp },
            mutableListOf<Marker>().apply {
                var ts = 0
//                addAll(listOf())
                add(createStatusMarker(Status.DRIVE, ++ts * 10))
                add(createStatusMarker(Status.WALK, ++ts * 10))
                add(createGeofenceMarker(++ts * 10))
                add(createStatusMarker(Status.DRIVE, ++ts * 10))
                add(createStatusMarker(Status.STOP, ++ts * 10))
//                add(createStatusMarker(Status.INACTIVE))
                add(createGeotagMarker(++ts * 10))
                add(createStatusMarker(Status.DRIVE, ++ts * 10))
                add(createStatusMarker(Status.OUTAGE, ++ts * 10))
            }
        )
    }

    private fun createStatusMarker(status: Status, plus: Int = 0): StatusMarker {
        var ts = plus
        return StatusMarker(
            MarkerType.STATUS,
            createTimestamp(++ts),
            createLocation(),
            createLocation(),
            createLocation(),
            createTimestamp(++ts),
            createTimestamp(++ts),
            createTimestamp(++ts),
            createTimestamp(++ts),
            status,
            (5 * 60 + Math.random() * 2000).toInt(),
            (1000 + Math.random() * 5000).toInt(),
            514,
            addr[plus / 10],
            "location_services_disabled"
        )
    }

    private fun createGeofenceMarker(plus: Int = 0): GeofenceMarker {
        var ts = plus
        return GeofenceMarker(
            MarkerType.GEOFENCE_ENTRY,
            createTimestamp(++ts),
            createLocation(),
            mapOf("name" to "Home"),
            createLocation(),
            createLocation(),
            createTimestamp(++ts),
            createTimestamp(++ts),
        )
    }

    private fun createGeotagMarker(plus: Int = 0): GeoTagMarker {
        return GeoTagMarker(
            MarkerType.GEOTAG,
            createTimestamp(plus),
            Location(0.0, 0.0),
            mapOf("type" to Constants.PICK_UP),
        )
    }

    private fun createTimestamp(plus: Int = 0) =
        ZonedDateTime.now().plusMinutes(plus.toLong() * 27).format(
            DateTimeFormatter.ISO_INSTANT
        )

    private fun createLocation() = Location(37.4750, -122.1709)

}