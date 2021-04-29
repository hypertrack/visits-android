package com.hypertrack.android.models.local

import com.hypertrack.android.api.Trip
import com.hypertrack.android.models.Order
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LocalTrip(
    val id: String,
    val status: TripStatus,
    val metadata: Map<String, String>,
    var orders: MutableList<LocalOrder>,
) {

    fun getOrder(orderId: String): LocalOrder? {
        return orders.firstOrNull { it.id == orderId }
    }

}

enum class TripStatus(val value: String) {
    ACTIVE("active"),
    COMPLETED("completed"),
    PROGRESSING_COMPLETION("processing_completion"),
    UNKNOWN("");

    companion object {
        fun fromString(str: String?): TripStatus {
            for (i in values()) {
                if (str == i.value) {
                    return i
                }
            }
            return UNKNOWN
        }
    }
}
