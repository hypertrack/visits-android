package com.hypertrack.android.models

import com.hypertrack.android.api.Geofence
import com.hypertrack.android.repository.*
import com.hypertrack.android.utils.OsUtilsProvider

data class Visit(val _id: String,
                 val visit_id: String = "", val driver_id: String = "", val customerNote: String = "",
                 val createdAt: String = "", val address: Address = Address(
        "",
        "",
        "",
        ""
    ),
                 val visitNote: String = "", var visitPicture: String = "",
                 var enteredAt:String = "",
                 val completedAt: String = "", val exitedAt: String = "",
                 val latitude: Double? = null, val longitude: Double? = null): VisitListItem() {
    val isCompleted: Boolean
        get() = status == COMPLETED

    val status: String
        get() = when {
                completedAt.isNotEmpty() -> COMPLETED
                enteredAt.isNotEmpty() -> VISITED
                else -> PENDING
            }

    val isLocal = !isNotLocal

    val isNotLocal:Boolean
        get() = (latitude != null && longitude != null)

    fun hasPicture() = visitPicture.isNotEmpty()

    fun hasNotes() = visitNote.isNotEmpty()

    fun update(geofence: Geofence) : Visit {

        return if (toNote(geofence.metadata) == customerNote) this
            else Visit(
            _id,
            visit_id,
            driver_id,
            toNote(geofence.metadata),
            createdAt,
            address,
            visitNote,
            visitPicture,
            enteredAt,
            completedAt,
            exitedAt,
            latitude,
            longitude
        )
        // TODO Denys - update when API adds support to geofence events
//        when {
//            (geofence.entered_at != enteredAt) -> pass
//            (geofence.exited_at != exitedAt) -> pass
//            (geofence.metadata.toString() != customerNote) -> pass
//            else -> return this
//        }
//        return Visit(_id, visit_id, driver_id, geofence.metadata.toString(),
//        createdAt, updatedAt, address, visitNote, visitPicture, geofence.entered_at,
//            completedAt, geofence.exited_at, latitude, longitude)

    }

    fun updateNote(newNote: String): Visit {
        return Visit(
            _id, visit_id, driver_id, customerNote,
            createdAt, address, newNote, visitPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude
        )
    }

    fun complete(completedAt: String): Visit {
        return Visit(
            _id, visit_id, driver_id, customerNote,
            createdAt, address, visitNote, visitPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude
        )
    }

    constructor(geofence: Geofence, osUtilsProvider: OsUtilsProvider) : this(
        _id = geofence.geofence_id,
        customerNote = toNote(geofence.metadata),
        address = osUtilsProvider.getAddressFromCoordinates(geofence.latitude, geofence.longitude),
        createdAt = geofence.created_at,
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
    latitude = geofence.latitude, longitude = geofence.longitude)

}

sealed class VisitListItem
data class HeaderVisitItem(val text: String) : VisitListItem()

private fun toNote(metadata: Map<String, Any>?): String {
    if (metadata == null) return ""
    val result = StringBuilder()
    metadata.forEach { (key, value) -> result.append("$key: $value\n") }
    return result.toString().dropLast(1)
}

data class Address (val street : String, val postalCode : String, val city : String, val country : String)