package com.hypertrack.android.models

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import com.hypertrack.android.decodeBase64Bitmap
import com.hypertrack.android.toBase64
import com.hypertrack.android.utils.AccountPreferencesProvider
import com.hypertrack.android.utils.OsUtilsProvider
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.temporal.ChronoUnit

@JsonClass(generateAdapter = true)
data class Visit(val _id: String,
                 val visit_id: String = "", val customerNote: String = "",
                 val createdAt: String = "", val address: Address = Address(
                "",
                "",
                "",
                ""
        ),
                 val visitNote: String = "", var visitPicture: String? = null,
                 var visitedAt: String? = null,
                 val completedAt: String = "", val exitedAt: String = "",
                 val latitude: Double? = null, val longitude: Double? = null,
                 val visitType: VisitType,
                 val _state: VisitStatus?,
                 var _icon: String? = null
) : VisitListItem() {

    val expectedLocation: Location?
        get() {
            if (visitType == VisitType.LOCAL) return null
            latitude?.let {
                longitude?.let {
                    val location = Location("visit")
                    location.longitude = longitude
                    location.latitude = latitude
                    return location

                }
            }
            return null
        }
    val state: VisitStatus
        get() = _state
                ?: if (visitType == VisitType.LOCAL) VisitStatus.VISITED else VisitStatus.PENDING
    val isEditable = state < VisitStatus.COMPLETED
    val isCompleted = state in listOf(VisitStatus.CANCELLED, VisitStatus.COMPLETED)
    val isLocal = visitType == VisitType.LOCAL
    val isVisited: Boolean
        get() = visitedAt?.isNotEmpty() == true
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

    val typeKey: String
        get() =
            when (visitType) {
                VisitType.TRIP -> "trip_id"
                VisitType.GEOFENCE -> "geofence_id"
                VisitType.LOCAL -> "visit_id"
            }


    val tripVisitPickedUp = state != VisitStatus.PENDING

    fun addBitmap(bitmap: Bitmap?) {
        _icon = bitmap?.toBase64()
    }

    fun getBitmap() = _icon?.decodeBase64Bitmap()

    fun hasPicture() = visitPicture?.isNotEmpty() ?: false

    fun hasNotes() = visitNote.isNotEmpty()

    fun update(prototype: VisitDataSource): Visit {
        // prototype can have visitedAt or metadata field updated that we need to copy or
        return if (prototype.customerNote == customerNote && prototype.visitedAt == visitedAt) this
        else Visit(
                _id,
                visit_id,
                prototype.customerNote,
                createdAt,
                address,
                visitNote,
                visitPicture,
                visitedAt = prototype.visitedAt,
                completedAt,
                exitedAt,
                latitude,
                longitude,
                visitType,
                _state = adjustState(state, prototype.visitedAt),
                _icon = _icon
        )

    }

    private fun adjustState(state: VisitStatus, visitedAt: String?) = when (state) {
        VisitStatus.PICKED_UP, VisitStatus.PENDING -> if (visitedAt != null) VisitStatus.VISITED else state
        else -> state
    }

    fun updateNote(newNote: String): Visit {
        return Visit(
                _id, visit_id, customerNote,
                createdAt, address, newNote, visitPicture, visitedAt,
                completedAt, exitedAt, latitude, longitude, visitType,
                state, _icon = _icon
        )
    }

    fun pickUp(newNote: String?) = moveToState(
            newState = VisitStatus.PICKED_UP,
            newNote = newNote
    )

    fun markVisited(newNote: String?) = moveToState(
            newState = VisitStatus.VISITED,
            newNote = newNote
    )

    fun complete(completedAt: String, newNote: String?) = moveToState(
            newState = VisitStatus.COMPLETED,
            transitionedAt = completedAt,
            newNote = newNote
    )

    fun cancel(cancelledAt: String, newNote: String?) = moveToState(
            newState = VisitStatus.CANCELLED,
            transitionedAt = cancelledAt,
            newNote = newNote
    )

    private fun moveToState(
            newState: VisitStatus,
            transitionedAt: String? = null,
            newNote: String?
    ) = Visit(
            _id, visit_id, customerNote,
            createdAt, address, newNote ?: visitNote, visitPicture, visitedAt,
            transitionedAt ?: completedAt, exitedAt, latitude, longitude, visitType,
            _state = newState, _icon = _icon
    )

    constructor(
            source: VisitDataSource,
            utils: OsUtilsProvider,
            preferences: AccountPreferencesProvider
    ) : this(
            _id = source._id,
            address = source.address
                    ?: utils.getAddressFromCoordinates(source.latitude, source.longitude),
            visit_id = "${utils.getString(source.visitNamePrefixId)} ${createSuffix(source, utils.getAddressFromCoordinates(source.latitude, source.longitude))}",
            customerNote = source.customerNote,
            createdAt = source.createdAt,
            visitedAt = source.visitedAt,
            latitude = source.latitude, longitude = source.longitude,
            visitType = source.visitType,
            _state =
            when {
                source.visitedAt.isNotEmpty() -> VisitStatus.VISITED
                preferences.isPickUpAllowed -> VisitStatus.PENDING
                else -> VisitStatus.PICKED_UP
            }
    )


}

private fun createSuffix(visitDataSource: VisitDataSource, address: Address) = if (visitDataSource.address != null) visitDataSource.visitNameSuffix else address.street


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

enum class VisitType { TRIP, GEOFENCE, LOCAL }

sealed class VisitListItem
data class HeaderVisitItem(val status: VisitStatusGroup) : VisitListItem()

@JsonClass(generateAdapter = true)
data class Address(val street: String, val postalCode: String?, val city: String?, val country: String?)

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
enum class VisitStatus {
    PENDING {
        override fun canTransitionTo(other: VisitStatus) = other > this
        override val group = VisitStatusGroup.PENDING_GROUP
    },
    PICKED_UP {
        override fun canTransitionTo(other: VisitStatus) = other > this
        override val group = VisitStatusGroup.PENDING_GROUP
    },
    VISITED {
        override fun canTransitionTo(other: VisitStatus) = other > this
        override val group = VisitStatusGroup.VISITED_GROUP
    },
    COMPLETED {
        override fun canTransitionTo(other: VisitStatus) = false
        override val group = VisitStatusGroup.COMPLETED_GROUP
    },
    CANCELLED {
        override fun canTransitionTo(other: VisitStatus) = false
        override val group = VisitStatusGroup.COMPLETED_GROUP
    };

    abstract fun canTransitionTo(other: VisitStatus): Boolean
    abstract val group: VisitStatusGroup
}

/** Group is coarse-grained status where some statuses and merged into one group */
enum class VisitStatusGroup {
    // Keep the order consistent with R.array.visit_state_group_names
    PENDING_GROUP, VISITED_GROUP, COMPLETED_GROUP
}
