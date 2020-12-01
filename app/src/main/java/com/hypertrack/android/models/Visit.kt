package com.hypertrack.android.models

import android.annotation.SuppressLint
import com.hypertrack.android.repository.COMPLETED
import com.hypertrack.android.repository.PENDING
import com.hypertrack.android.repository.VISITED
import com.hypertrack.android.utils.OsUtilsProvider
import java.time.Instant
import java.time.temporal.ChronoUnit

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
                 val visitType: VisitType, private var _tripVisitPickedUp: Boolean = false,
                 val state: VisitStatus = if (visitType == VisitType.LOCAL) VisitStatus.VISITED else VisitStatus.PENDING
 ): VisitListItem() {
    val isEditable: Boolean = (state in listOf(VisitStatus.PICKED_UP, VisitStatus.PENDING, VisitStatus.VISITED))
    val isCompleted: Boolean
        get() = status == COMPLETED

    val status: String
        get() = when {
            completedAt.isNotEmpty() -> COMPLETED
            enteredAt.isNotEmpty() -> VISITED
            else -> PENDING
        }

    val isLocal = visitType == VisitType.LOCAL

    val isDeletable: Boolean
        get() {
            return !isLocal &&
                    !(visitType == VisitType.TRIP && isOngoingOrCompletedRecently())
        }

    private fun isOngoingOrCompletedRecently(): Boolean {
        if (completedAt.isEmpty()) return true
        return try {
            completedAt.isLaterThanADayAgo()
        } catch (ignored: Throwable) {
            false
        }
    }

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

    fun complete(completedAt: String) = moveToState(VisitStatus.COMPLETED, completedAt)

    fun pickUp() = moveToState(VisitStatus.PICKED_UP)

    fun cancel() = moveToState(VisitStatus.CANCELLED)

    fun markVisited() = moveToState(VisitStatus.VISITED)

    private fun moveToState(newState: VisitStatus, completionTime: String? = null): Visit {
        return Visit(
            _id, visit_id, customerNote,
            createdAt, address, visitNote, visitPicture, enteredAt,
            completionTime?:completedAt, exitedAt, latitude, longitude, visitType, _tripVisitPickedUp, state = newState
        )
    }



    constructor(visitDataSource: VisitDataSource, osUtilsProvider: OsUtilsProvider) : this(
        _id = visitDataSource._id,
        visit_id = "${osUtilsProvider.getStringResourceForId(visitDataSource.visitNamePrefixId)} ${visitDataSource.visitNameSuffix}",
        customerNote = visitDataSource.customerNote,
        address = visitDataSource.address ?: osUtilsProvider.getAddressFromCoordinates(visitDataSource.latitude, visitDataSource.longitude),
        createdAt = visitDataSource.createdAt,
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
        latitude = visitDataSource.latitude, longitude = visitDataSource.longitude,
        visitType = visitDataSource.visitType
    )

}

@SuppressLint("NewApi")
private fun String.isLaterThanADayAgo(): Boolean =
    Instant.parse(this).isAfter(Instant.now().minus(1, ChronoUnit.DAYS))

interface VisitDataSource {
    val _id: String
    val customerNote: String
    val address: Address?
    val createdAt: String
    val latitude: Double
    val longitude: Double
    val visitType: VisitType
    val visitNamePrefixId: Int
    val visitNameSuffix: String
}

enum class VisitType {
    TRIP, GEOFENCE, LOCAL
}

sealed class VisitListItem
data class HeaderVisitItem(val text: String) : VisitListItem()

data class Address (val street : String, val postalCode : String, val city : String, val country : String)

/**
 *
 * Trip and Geofence based visits are in _Pending_ state when they received from the backend.
 * From this state they are eligible for "PICK_UP" and "CHECK_IN" actions that corresponds to
 * receiving deliverable and attending visit destination. The former doesn't change it's sorting
 * the visit is still _Pending_ while the latter moves it to _Visited_ bucket. Local visits
 * are created in _Visited_ bucket (they are automatically *Checked In*) and cannot be cancelled,
 * while other _Visited_ items could be cancelled ("CANCEL" action) or completed
 * ("CHECK_OUT" action).
 *
 */

enum class VisitStatus { PENDING, PICKED_UP, VISITED, COMPLETED, CANCELLED }