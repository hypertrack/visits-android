package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.TripModel.Companion.fromTrip
import com.hypertrack.sdk.views.dao.Trip
import com.hypertrack.sdk.views.maps.TripSubscription
import java.util.*

internal class TrackingState(context: Context?) : BaseState(context!!) {
    var selectedTripId: String?
        private set
    val isHomeLatLngAdded: Boolean
    private var mTripModel: TripModel? = null
    val trips: MutableMap<String, Trip> = HashMap()
    val tripSubscription: MutableMap<String, TripSubscription> = HashMap()
    fun setSelectedTrip(trip: Trip?) {
        if (trip != null) {
            selectedTripId = trip.tripId
            sharedHelper.setSelectedTripId(selectedTripId!!)
            mTripModel = fromTrip(trip)
        } else {
            selectedTripId = null
            mTripModel = null
            sharedHelper.clearSelectedTripId()
        }
    }

    val allTripsStartingFromLatest: List<Trip>
        get() {
            val result = ArrayList(trips.values)
            Collections.sort(result) { trip1: Trip, trip2: Trip ->
                if (trip1.startDate == null) return@sort 1
                trip1.startDate!!.compareTo(trip2.startDate)
            }
            return result
        }
    val shareMessage: String
        get() = if (mTripModel == null) "" else mTripModel!!.shareableMessage

    init {
        selectedTripId = sharedHelper.selectedTripId
        isHomeLatLngAdded = sharedHelper.isHomePlaceSet
    }
}