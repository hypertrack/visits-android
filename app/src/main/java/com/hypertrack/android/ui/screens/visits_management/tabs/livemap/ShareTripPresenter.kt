package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.hypertrack.android.models.TripCompletionSuccess
import com.hypertrack.android.models.TripCompletionError
import com.hypertrack.android.models.TripManagementApi
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.MapUtils.getBuilder
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.TrackingPresenter.Companion.shareAction
import com.hypertrack.maps.google.widget.GoogleMapAdapter
import com.hypertrack.sdk.views.DeviceUpdatesHandler
import com.hypertrack.sdk.views.HyperTrackViews
import com.hypertrack.sdk.views.dao.Location
import com.hypertrack.sdk.views.dao.StatusUpdate
import com.hypertrack.sdk.views.dao.Trip
import com.hypertrack.sdk.views.maps.HyperTrackMap
import kotlinx.coroutines.launch

internal class ShareTripPresenter(
    private val context: Context,
    private val view: View,
    url: String,
    private val backendProvider: TripManagementApi,
    deviceId: String,
    realTimeUpdatesProvider: HyperTrackViews,
    private val viewLifecycleOwner: LifecycleOwner
) : DeviceUpdatesHandler {
    private val state: ShareTripState = ShareTripState(context, url)
    private val hyperTrackDeviceId: String  = deviceId
    private val hyperTrackViews: HyperTrackViews = realTimeUpdatesProvider
    private var hyperTrackMap: HyperTrackMap? = null

    fun subscribeTripUpdates(googleMap: GoogleMap, tripId: String?) {
        if (hyperTrackMap == null) {
            val mapAdapter = GoogleMapAdapter(googleMap, getBuilder(context).build())
            hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter)
            hyperTrackMap?.setMyLocationEnabled(false)
        }
        state.currentTripId = tripId
        hyperTrackViews.subscribeToDeviceUpdates(hyperTrackDeviceId, tripId!!, this)
        hyperTrackMap?.bind(hyperTrackViews, hyperTrackDeviceId)
            ?.subscribeTrip(tripId)
    }

    fun pause() {
        hyperTrackViews.unsubscribeFromDeviceUpdates(this)
        hyperTrackMap!!.unbindHyperTrackViews()
    }

    fun shareTrackMessage() {
        shareAction(context, state.shareMessage)
    }

    fun endTrip() {
        state.currentTripId?.let { tripId ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = backendProvider.completeTrip(tripId)) {
                    is TripCompletionSuccess -> {
                        Log.d(TAG, "trip is ended")
                        state.currentTripId = null
                    }
                    is TripCompletionError -> {
                        Log.e(TAG, "trip completion failure", result.error)
                        Toast.makeText(context, "Trip completion failure", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun destroy() {
        hyperTrackViews.unsubscribeFromDeviceUpdates(this)
        hyperTrackMap?.destroy()
        hyperTrackMap = null
    }

    override fun onLocationUpdateReceived(location: Location) {}
    override fun onBatteryStateUpdateReceived(i: Int) {}
    override fun onStatusUpdateReceived(statusUpdate: StatusUpdate) {}
    override fun onTripUpdateReceived(trip: Trip) {
        hyperTrackMap?.moveToTrip(trip)
        state.updateTrip(trip)
        view.onTripUpdate(trip)
    }

    override fun onError(e: Exception, s: String) {}
    override fun onCompleted(s: String) {}
    interface View {
        fun showProgressBar()
        fun hideProgressBar()
        fun onTripUpdate(trip: Trip)
    }

    companion object {
        private const val TAG = "ShTripPresenter"
    }

}