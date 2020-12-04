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
                 var visitedAt:String = "",
                 val completedAt: String = "", val exitedAt: String = "",
                 val latitude: Double? = null, val longitude: Double? = null,
                 val visitType: VisitType,
                 val state: VisitStatus = if (visitType == VisitType.LOCAL) VisitStatus.VISITED else VisitStatus.PENDING
 ): VisitListItem() {
    val isEditable: Boolean = (state in listOf(VisitStatus.PICKED_UP, VisitStatus.PENDING, VisitStatus.VISITED))
    val isCompleted: Boolean
        get() = status == COMPLETED

    val status: String
        get() = when {
            completedAt.isNotEmpty() -> COMPLETED
            visitedAt.isNotEmpty() -> VISITED
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


    val tripVisitPickedUp = state != VisitStatus.PENDING

    fun hasPicture() = visitPicture.isNotEmpty()

    fun hasNotes() = visitNote.isNotEmpty()

    fun update(prototype: VisitDataSource) : Visit {
        // prototype can have visitedAt field that we need to copy or
        return if (prototype.customerNote == customerNote && prototype.visitedAt == visitedAt) this
            else Visit(
            _id,
            visit_id,
            prototype.customerNote,
            createdAt,
            address,
            visitNote,
            visitPicture,
            visitedAt = prototype.visitedAt?:visitedAt,
            completedAt,
            exitedAt,
            latitude,
            longitude,
            visitType,
            state = adjustState(state, prototype.visitedAt)
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

    private fun adjustState(state: VisitStatus, visitedAt: String?): VisitStatus {
        val visitStatus = when (state) {
            VisitStatus.PICKED_UP, VisitStatus.PENDING -> if (visitedAt != null) VisitStatus.VISITED else state
            else -> state
        }
        return visitStatus
    }

    fun updateNote(newNote: String): Visit {
        return Visit(
            _id, visit_id, customerNote,
            createdAt, address, newNote, visitPicture, visitedAt,
            completedAt, exitedAt, latitude, longitude, visitType,
            state
        )
    }

    fun complete(completedAt: String) = moveToState(VisitStatus.COMPLETED, completedAt)

    fun pickUp() = moveToState(VisitStatus.PICKED_UP)

    fun cancel(cancelledAt: String) = moveToState(VisitStatus.CANCELLED, cancelledAt)

    fun markVisited() = moveToState(VisitStatus.VISITED)

    private fun moveToState(newState: VisitStatus, transitionedAt: String? = null): Visit {
        return Visit(
            _id, visit_id, customerNote,
            createdAt, address, visitNote, visitPicture, visitedAt,
            transitionedAt?:completedAt, exitedAt, latitude, longitude, visitType, state = newState
        )
    }



    constructor(visitDataSource: VisitDataSource, osUtilsProvider: OsUtilsProvider) : this(
        _id = visitDataSource._id,
        visit_id = "${osUtilsProvider.getStringResourceForId(visitDataSource.visitNamePrefixId)} ${visitDataSource.visitNameSuffix}",
        customerNote = visitDataSource.customerNote,
        address = visitDataSource.address ?: osUtilsProvider.getAddressFromCoordinates(visitDataSource.latitude, visitDataSource.longitude),
        createdAt = visitDataSource.createdAt,
        visitedAt = visitDataSource.visitedAt,
        latitude = visitDataSource.latitude, longitude = visitDataSource.longitude,
        visitType = visitDataSource.visitType,
        state = if (visitDataSource.visitedAt.isEmpty()) VisitStatus.PENDING else VisitStatus.VISITED
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
    val visitedAt: String
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