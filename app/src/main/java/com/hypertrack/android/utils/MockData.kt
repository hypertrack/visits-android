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

    private val addr = listOf(
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

    private val MOCK_HISTORY_JSON: String by lazy {
        MyApplication.context.resources.openRawResource(R.raw.mock_history).bufferedReader()
            .use { it.readText() }
    }

    private val MOCK_SUMMARY = Summary(
        totalDistance = 12 * 857,
        totalDuration = 3 * 57 * 58,
        totalDriveDistance = 4 * 997,
        totalDriveDuration = 1 * 59 * 57,
        stepsCount = 3794,
        totalWalkDuration = 2 * 57 * 59,
        totalStopDuration = 20 * 57,
    )

    val MOCK_HISTORY: HistoryResult by lazy {
        Injector.getMoshi().adapter(HistoryResponse::class.java)
            .fromJson(MOCK_HISTORY_JSON)!!.let { h: HistoryResponse ->
                History(
                    MOCK_SUMMARY,
                    h.locations.coordinates.map {
                        Location(
                            it.longitude,
                            it.latitude
                        ) to it.timestamp
                    },
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

    const val MOCK_GEOFENCES_JSON =
        "{\"data\":[{\"geofence_id\":\"79eb01c2-1f86-4fc6-9200-fa10b7221455\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"single_use\":false,\"created_at\":\"2021-05-12T14:05:11.555811+00:00\",\"metadata\":{\"name\":\"Warehouse\",\"address\":\"Mountain View,  Showers Drive, 190\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.10794627666472,37.40846911725437]},\"archived\":false,\"geofence_type\":\"device\",\"radius\":100},{\"geofence_id\":\"063f1527-9753-468c-827f-55e7830a13db\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"single_use\":false,\"created_at\":\"2021-05-12T13:27:49.301081+00:00\",\"metadata\":{\"name\":null,\"address\":\"Palo Alto,  Alma Street, 2879\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.13376723229887,37.42496597636671]},\"archived\":false,\"geofence_type\":\"device\",\"radius\":100,\"markers\":{\"data\":[{\"marker_id\":\"cee17656-9ba0-4fe0-9064-066f90bda786\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"trip_id\":null,\"created_at\":\"2021-05-12T14:03:15.341Z\",\"arrival\":{\"recorded_at\":\"2021-05-12T13:23:15.341Z\",\"location\":{\"coordinates\":[-122.134206,37.424741,-6.33],\"type\":\"Point\"}},\"exit\":{\"recorded_at\":\"2021-05-12T14:05:35.972Z\",\"location\":{\"coordinates\":[-122.134227,37.424749,3.22],\"type\":\"Point\"}},\"outage_event\":null,\"geofence_type\":\"device\",\"geofence_id\":\"063f1527-9753-468c-827f-55e7830a13db\",\"metadata\":{\"name\":null,\"address\":\"Palo Alto,  Alma Street, 2879\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.13376723229887,37.42496597636671]},\"route_to\":{\"duration\":10000,\"distance\":10000,\"idle_time\":0,\"started_at\":\"2021-05-12T14:03:14.423Z\"},\"radius\":100,\"duration\":2520},{\"marker_id\":\"ff6334eb-60d3-435a-849a-3541f660c471\",\"device_id\":\"A42883CF-AD98-322C-AA49-95EE83EF295B\",\"account_id\":\"d9ad00f9-ee2c-4d3c-9769-9c333bea6519\",\"trip_id\":null,\"created_at\":\"2021-05-12T13:57:13.684Z\",\"arrival\":{\"recorded_at\":\"2021-05-11T11:57:13.684Z\",\"location\":{\"coordinates\":[-122.134201,37.424739,3.77],\"type\":\"Point\"}},\"exit\":{\"recorded_at\":\"2021-05-11T12:00:00.278Z\",\"location\":null},\"outage_event\":null,\"geofence_type\":\"device\",\"geofence_id\":\"063f1527-9753-468c-827f-55e7830a13db\",\"metadata\":{\"name\":null,\"address\":\"Palo Alto,  Alma Street, 2879\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.13376723229887,37.42496597636671]},\"route_to\":{\"duration\":5200,\"distance\":4300,\"idle_time\":0,\"started_at\":\"2021-05-12T13:57:12.897Z\"},\"radius\":100,\"duration\":180}],\"pagination_token\":null,\"links\":{\"next\":null}}}],\"pagination_token\":null,\"links\":{\"next\":null}}"

    const val MOCK_INTEGRATIONS_RESPONSE =
        "{\"data\":[{\"name\":null,\"id\":\"4441136372\"},{\"name\":\"100X.VC\",\"id\":\"5231315202\"},{\"name\":\"1Password\",\"id\":\"4950449208\"},{\"name\":\"4MATIV\",\"id\":\"4441162290\"},{\"name\":\"A&B Courier Service Limited\",\"id\":\"4441148258\"},{\"name\":\"A/B Smartly\",\"id\":\"5860436823\"},{\"name\":\"Aaj Tak\",\"id\":\"5225918494\"},{\"name\":\"abatox.hu\",\"id\":\"4441135165\"},{\"name\":\"Able\",\"id\":\"4441135151\"},{\"name\":\"Accenture Limited\",\"id\":\"4441136368\"},{\"name\":\"Acuity\",\"id\":\"4547060422\"},{\"name\":\"ADESA\",\"id\":\"6094023069\"},{\"name\":\"Advantary\",\"id\":\"4647658256\"},{\"name\":\"advengersmedia.com\",\"id\":\"4441123975\"},{\"name\":\"Advent Trinity\",\"id\":\"5224222119\"},{\"name\":\"adyog\",\"id\":\"4441135170\"},{\"name\":\"Afn\",\"id\":\"5894607104\"},{\"name\":\"AfricaSokoni\",\"id\":\"4441136383\"},{\"name\":\"ahuja.email\",\"id\":\"5776265606\"},{\"name\":\"AI Software Company\",\"id\":\"4912961151\"},{\"name\":\"Airbase\",\"id\":\"4493511265\"},{\"name\":\"Airtel\",\"id\":\"5626580327\"},{\"name\":\"Aita\",\"id\":\"4804557609\"},{\"name\":\"ALANTRA\",\"id\":\"5094378652\"},{\"name\":\"Albatrot\",\"id\":\"4874606289\"},{\"name\":\"Alberto Gal\\u00e1n\",\"id\":\"4441135150\"},{\"name\":\"Albertsons\",\"id\":\"5102828712\"},{\"name\":\"Alfred Colombia\",\"id\":\"5008107555\"},{\"name\":\"Algo Scientific\",\"id\":\"4441123973\"},{\"name\":\"Alliance of Chief Executives\",\"id\":\"4620135151\"},{\"name\":\"Allsopp & Allsopp\",\"id\":\"4441162280\"},{\"name\":\"Altus Global Trade Solutions\",\"id\":\"5028460957\"},{\"name\":\"Amazon\",\"id\":\"4568501748\"},{\"name\":\"Amazon\",\"id\":\"4876163965\"},{\"name\":\"Amazon\",\"id\":\"5577668465\"},{\"name\":\"Amazon.com\",\"id\":\"5810269778\"},{\"name\":\"American Society of Furniture Designers\",\"id\":\"6077764411\"},{\"name\":\"Americana Group\",\"id\":\"4535979648\"},{\"name\":\"amplifiercorporation.com\",\"id\":\"5413739190\"},{\"name\":\"AngelList\",\"id\":\"4777997295\"},{\"name\":\"AngelList\",\"id\":\"5713976065\"},{\"name\":\"ANI Technologies\",\"id\":\"5134132501\"},{\"name\":\"Anirit Urban Agrofoods\",\"id\":\"5349470225\"},{\"name\":\"annumtechnologies.co\",\"id\":\"5912370092\"},{\"name\":\"Apax Partners\",\"id\":\"5602819213\"},{\"name\":\"API2Cart\",\"id\":\"5603200832\"},{\"name\":\"API3\",\"id\":\"5200513113\"},{\"name\":\"App Innovation Technologies\",\"id\":\"4441136375\"},{\"name\":\"Appify\",\"id\":\"4676237558\"},{\"name\":\"Apple Inc.\",\"id\":\"4871616142\"},{\"name\":\"Apple Inc.\",\"id\":\"4925000375\"},{\"name\":\"Arch Grants\",\"id\":\"5767142815\"},{\"name\":\"archipel.tech\",\"id\":\"4452595627\"},{\"name\":\"Arrowroot Capital\",\"id\":\"5394439929\"},{\"name\":\"Article Writing\",\"id\":\"5183590425\"},{\"name\":\"Arun Gas\",\"id\":\"5338990157\"},{\"name\":\"asdf\",\"id\":\"6077872762\"},{\"name\":\"Ashika Stock Broking Limited\",\"id\":\"4556268517\"},{\"name\":\"ASSOCHAM\",\"id\":\"4791497603\"},{\"name\":\"Atlantic Cargo Pvt. Ltd.\",\"id\":\"6017237026\"},{\"name\":\"automonkey\",\"id\":\"5209410081\"},{\"name\":\"Autonomic\",\"id\":\"5923524676\"},{\"name\":\"Autosys Industrial Solutions Private Limited\",\"id\":\"5838194705\"},{\"name\":\"Avataar Venture Partners\",\"id\":\"5378716080\"},{\"name\":\"Avendus\",\"id\":\"4640555526\"},{\"name\":\"Axis Bank\",\"id\":\"5310293148\"},{\"name\":\"BajuGali\",\"id\":\"4751763111\"},{\"name\":\"Balabing\",\"id\":\"4441136382\"},{\"name\":\"Bank of Montreal\",\"id\":\"5669629354\"},{\"name\":\"BASIC\",\"id\":\"5458196529\"},{\"name\":\"basig.me\",\"id\":\"4441123976\"},{\"name\":\"bcg\",\"id\":\"4525992500\"},{\"name\":\"Beacons Point, Inc.\",\"id\":\"4683046751\"},{\"name\":\"becfertilizers.in\",\"id\":\"4715736362\"},{\"name\":\"bectron.in\",\"id\":\"5834053024\"},{\"name\":\"Beintoo\",\"id\":\"6092456759\"},{\"name\":\"belolabs.com\",\"id\":\"5757784964\"},{\"name\":\"berkeley college\",\"id\":\"4646156932\"},{\"name\":\"Berkeley Unified School District\",\"id\":\"4527698510\"},{\"name\":\"Berkshire Hathaway GUARD Insurance Companies\",\"id\":\"5269323666\"},{\"name\":\"better.network\",\"id\":\"4441135171\"},{\"name\":\"Beyond Type 1\",\"id\":\"4515823168\"},{\"name\":\"Bhilai Engineering Corporation Limited\",\"id\":\"4715761730\"},{\"name\":\"Bhilai Engineering Corporation Limited\",\"id\":\"4719101830\"},{\"name\":\"bi2ai\",\"id\":\"4886971594\"},{\"name\":\"bigdatamartech\",\"id\":\"4505441949\"},{\"name\":\"Bilanz Capital Advisors\",\"id\":\"5117707488\"},{\"name\":\"billiondevices.com\",\"id\":\"6023782063\"},{\"name\":\"BitCot\",\"id\":\"5338990182\"},{\"name\":\"Blackvt\",\"id\":\"5025996337\"},{\"name\":\"Block Gemini\",\"id\":\"4441138712\"},{\"name\":\"Blowhorn\",\"id\":\"5395978031\"},{\"name\":\"BMSCE\",\"id\":\"4441136371\"},{\"name\":\"Bond\",\"id\":\"4515681734\"},{\"name\":\"Box\",\"id\":\"5838013270\"},{\"name\":\"Brado\",\"id\":\"5150108550\"},{\"name\":\"brego.ai\",\"id\":\"5310279314\"},{\"name\":\"Brex\",\"id\":\"4488376430\"},{\"name\":\"Bridge\",\"id\":\"5388281110\"},{\"name\":\"Brightgrove\",\"id\":\"4640375932\"}],\"after\":\"100\"}"

}