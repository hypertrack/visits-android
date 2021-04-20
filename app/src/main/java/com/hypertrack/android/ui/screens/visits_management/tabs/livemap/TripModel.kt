package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import com.hypertrack.sdk.views.dao.Trip
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

class TripModel private constructor(val tripId: String, private val shareableUrl: String) {
    private var mRemainingDuration: Int? = null
    private var mTrip: Trip? = null
    private var tripReceived: LocalTime? = null

    val shareableMessage: String
        get() = ShareableMessage(shareableUrl, mRemainingDuration, tripReceived).shareMessage

    fun update(trip: Trip?) {
        if (trip == null || tripId != trip.tripId) return
        mTrip = trip
        tripReceived = LocalTime.now()
        mRemainingDuration = when (val route = trip.estimate?.route) {
            null -> null
            else -> route.remainingDuration
        }
    }

    internal class ShareableMessage(
        private val shareUrl: String,
        private val remainingDuration: Int?,
        private val adjustmentTime: LocalTime?
    ) {
        val shareMessage: String
            get() {
                if (remainingDuration == null) {
                    return "Track my live location here $shareUrl"
                }
                val arriveTime =
                    adjustmentTime!!.plus(remainingDuration.toLong(), ChronoUnit.SECONDS)
                return if (arriveTime.isBefore(LocalTime.now())) {
                    "Arriving now. Track my live location here $shareUrl"
                } else "Will be there by ${arriveTime.format(DateTimeFormatter.ofPattern("h:mma"))}. Track my live location here $shareUrl"

            }
    }

    companion object {
        @JvmStatic
        fun fromTrip(trip: Trip): TripModel? {
            val views = trip.views
            if (views.sharedUrl == null) return null
            val model = TripModel(trip.tripId, views.sharedUrl!!)
            model.update(trip)
            return model
        }
    }
}