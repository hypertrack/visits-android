package com.hypertrack.android.models

import com.hypertrack.android.repository.COMPLETED
import com.hypertrack.android.repository.PENDING
import com.hypertrack.android.repository.VISITED
import com.hypertrack.android.utils.OsUtilsProvider

data class Visit(val _id: String,
                 val visit_id: String = "", val customerNote: String = "",
                 val createdAt: String = "", val address: Address = Address(
        "",
        "",
        "",
        ""
    ),
                 val visitNote: String = "", var visitPicture: String = "",
                 var enteredAt:String = "",
                 val completedAt: String = "", val exitedAt: String = "",
                 val latitude: Double? = null, val longitude: Double? = null,
                 val visitType: VisitType, private var _tripVisitPickedUp: Boolean = false
 ): VisitListItem() {
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

    val typeKey:String
        get() =
            when (visitType) {
                VisitType.TRIP -> "trip_id"
                VisitType.GEOFENCE -> "geofence_id"
                VisitType.LOCAL -> "visit_id"
            }


    var tripVisitPickedUp: Boolean?
        get() = if (visitType == VisitType.TRIP) _tripVisitPickedUp else null
        set(value) { _tripVisitPickedUp = value ?: false }

    fun hasPicture() = visitPicture.isNotEmpty()

    fun hasNotes() = visitNote.isNotEmpty()

    fun update(prototype: VisitDataSource) : Visit {

        return if (prototype.customerNote == customerNote) this
            else Visit(
            _id,
            visit_id,
            prototype.customerNote,
            createdAt,
            address,
            visitNote,
            visitPicture,
            enteredAt,
            completedAt,
            exitedAt,
            latitude,
            longitude,
            visitType,
            _tripVisitPickedUp
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
            _id, visit_id, customerNote,
            createdAt, address, newNote, visitPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude, visitType, _tripVisitPickedUp
        )
    }

    fun complete(completedAt: String): Visit {
        return Visit(
            _id, visit_id, customerNote,
            createdAt, address, visitNote, visitPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude, visitType, _tripVisitPickedUp
        )
    }

    constructor(visitDataSource: VisitDataSource, osUtilsProvider: OsUtilsProvider) : this(
        _id = visitDataSource._id,
        visit_id = visitDataSource.visitId,
        customerNote = visitDataSource.customerNote,
        address = visitDataSource.address ?: osUtilsProvider.getAddressFromCoordinates(visitDataSource.latitude, visitDataSource.longitude),
        createdAt = visitDataSource.createdAt,
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
        latitude = visitDataSource.latitude, longitude = visitDataSource.longitude,
        visitType = visitDataSource.visitType
    )

}

interface VisitDataSource {
    val _id: String
    val visitId: String
    val customerNote: String
    val address: Address?
    val createdAt: String
    val latitude: Double
    val longitude: Double
    val visitType: VisitType
}

enum class VisitType {
    TRIP, GEOFENCE, LOCAL
}

sealed class VisitListItem
data class HeaderVisitItem(val text: String) : VisitListItem()

data class Address (val street : String, val postalCode : String, val city : String, val country : String)