package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.TripModel.Companion.fromTrip
import com.hypertrack.sdk.views.dao.Trip

internal class ShareTripState(
    context: Context,
    val url: String
    ) : BaseState(context) {
    private var tripId = sharedHelper.createdTripId
    private var mTripModel: TripModel? = null

    var currentTripId: String?
        get() = tripId
        set(value) {
            tripId = value
            sharedHelper.createdTripId = value
        }

    fun updateTrip(trip: Trip) {
        mTripModel = fromTrip(trip)
    }

    val shareMessage: String
        get() = mTripModel?.shareableMessage ?: url
}