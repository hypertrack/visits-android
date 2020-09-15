package com.hypertrack.android.models

import com.hypertrack.android.api.Geofence
import com.hypertrack.android.repository.*
import com.hypertrack.android.toNote
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

        return if (geofence.metadata.toNote() == customerNote) this
            else Visit(
            _id,
            visit_id,
            driver_id,
            geofence.metadata.toNote(),
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

    constructor(visitDataSource: VisitDataSource, osUtilsProvider: OsUtilsProvider) : this(
        _id = visitDataSource.visitId,
        customerNote = visitDataSource.customerNote,
        address = visitDataSource.address ?: osUtilsProvider.getAddressFromCoordinates(visitDataSource.latitude, visitDataSource.longitude),
        createdAt = visitDataSource.createdAt,
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
    latitude = visitDataSource.latitude, longitude = visitDataSource.longitude)

}

interface VisitDataSource {
    val visitId: String
    val customerNote: String
    val address: Address?
    val createdAt: String
    val latitude: Double
    val longitude: Double
}

sealed class VisitListItem
data class HeaderVisitItem(val text: String) : VisitListItem()

data class Address (val street : String, val postalCode : String, val city : String, val country : String)