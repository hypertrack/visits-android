package com.hypertrack.android.models

import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.api.TripDestination
import com.hypertrack.android.models.local.OrderStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Suppress("UNCHECKED_CAST")
@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "order_id") val id: String,
    val destination: TripDestination,
    @Json(name = "status") val _status: String,
    @Json(name = "scheduled_at") val scheduledAt: String?,
    val estimate: Estimate?,
    @Json(name = "metadata") val _metadata: Map<String, Any>?,
) {

    val status: OrderStatus
        get() = OrderStatus.fromString(_status)

    val metadata: Map<String, Any>
        get() = (_metadata ?: mapOf())


}

@JsonClass(generateAdapter = true)
data class Estimate(
    @Json(name = "arrive_at") val arriveAt: String?,
    @field:Json(name = "route") val route: Route?
)

@JsonClass(generateAdapter = true)
data class Route(
    @field:Json(name = "remaining_duration") val remainingDuration: Int?
)


/*
* {
  "account_id": "3d58f8b3-45fc-49be-828d-b7cc81afdd3b",
  "order_id": "1",
  "metadata": null,
  "scheduled_at": "2000-01-01T00:00:00.000Z",
  "device_id": "252CB4DA-03EE-36D9-9081-952F8FD73BA5",
  "destination": {
    "address": "1",
    "geometry": {
      "type": "Point",
      "coordinates": [
        -95.4604032,
        37.6430412
      ]
    },
    "radius": 30
  },
  "share_url": "https://trck.at/vROKAOq",
  "status": "ongoing",
  "started_at": "2021-03-24T13:54:50.600Z",
  "service_time": 0,
  "delayed": null,
  "eta_relevance_data": {
    "reason": "Destination too far",
    "time_at_irrelevance": "2021-03-24T13:55:00.749Z",
    "status": false
  }
}
*
*
*
*
*
* */