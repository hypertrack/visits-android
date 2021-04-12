package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import com.hypertrack.backend.models.ShareableTrip
import com.hypertrack.sdk.views.dao.Trip
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

class TripModel private constructor(val tripId: String, val shareableUrl: String) {
    private var mRemainingDuration: Int? = null
    private var mTrip: Trip? = null
    private var tripReceived: LocalTime? = null
    val shareableMessage: String
        get() = ShareableMessage(shareableUrl, mRemainingDuration, tripReceived).shareMessage

    fun update(trip: Trip?) {
        if (trip == null || tripId != trip.tripId) return
        mTrip = trip
        tripReceived = LocalTime.now()
        mRemainingDuration = if (trip.estimate == null) null else if (trip.estimate!!
                .route == null
        ) null else trip.estimate!!.route!!.remainingDuration
    }

    internal class ShareableMessage(
        private val mShareableUrl: String,
        private val mRemainingDuration: Int?,
        private val mDurationAdjustmentTime: LocalTime?
    ) {
        val shareMessage: String
            get() {
                if (mRemainingDuration == null) {
                    return String.format("Track my live location here %s", mShareableUrl)
                }
                assert(mDurationAdjustmentTime != null)
                val arriveTime =
                    mDurationAdjustmentTime!!.plus(mRemainingDuration.toLong(), ChronoUnit.SECONDS)
                return if (arriveTime.isBefore(LocalTime.now())) {
                    String.format("Arriving now. Track my live location here %s", mShareableUrl)
                } else String.format(
                    "Will be there by %s. Track my live location here %s",
                    arriveTime.format(DateTimeFormatter.ofPattern("h:mma")),
                    mShareableUrl
                )
            }
    }

    companion object {
        fun fromShareableTrip(shareableTrip: ShareableTrip): TripModel {
            val model = TripModel(shareableTrip.tripId, shareableTrip.shareUrl)
            val remainingDuration = shareableTrip.remainingDuration
            if (null != remainingDuration) {
                model.mRemainingDuration = remainingDuration
                model.tripReceived = LocalTime.now()
            }
            return model
        }

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