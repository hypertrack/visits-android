package com.hypertrack.android

import com.hypertrack.android.api.Point
import com.hypertrack.android.api.Trip
import com.hypertrack.android.api.TripDestination
import com.hypertrack.android.api.Views
import com.hypertrack.android.models.Estimate
import com.hypertrack.android.models.Order
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.models.local.TripStatus

fun createBaseOrder(): Order {
    return Order(
        "order " + Math.random(),
        TripDestination(
            null,
            Point(listOf(42.0, 42.0)),
            arrivedAt = "2020-02-02T20:20:02.020Z"
        ),
        OrderStatus.ONGOING.value,
        "2020-02-02T20:20:02.020Z",
        Estimate("2020-02-02T20:20:02.020Z", null),
        null,
    )
}

fun createBaseTrip(): Trip {
    return Trip(
        tripId = "tripId " + Math.random(),
        status = TripStatus.ACTIVE.value,
        orders = null,
        views = Views("", null),
        createdAt = "",
        metadata = emptyMap(),
        destination = TripDestination(
            null,
            Point(listOf(42.0, 42.0)),
            arrivedAt = "2020-02-02T20:20:02.020Z"
        ),
        estimate = Estimate(
            "",
            null
        )
    )
}

