package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.MapUtils.getBuilder
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.backend.AbstractBackendProvider
import com.hypertrack.backend.ResultHandler
import com.hypertrack.logistics.android.github.R
import com.hypertrack.maps.google.widget.GoogleMapAdapter
import com.hypertrack.sdk.views.DeviceUpdatesHandler
import com.hypertrack.sdk.views.HyperTrackViews
import com.hypertrack.sdk.views.dao.Location
import com.hypertrack.sdk.views.dao.StatusUpdate
import com.hypertrack.sdk.views.dao.Trip
import com.hypertrack.sdk.views.maps.GpsLocationProvider
import com.hypertrack.sdk.views.maps.HyperTrackMap
import java.util.*

internal class TrackingPresenter(
    private val context: Context,
    private val view: View,
    private val backendProvider: AbstractBackendProvider,
    private val hyperTrackService: HyperTrackService,
    private val realTimeUpdatesService: HyperTrackViews
) : DeviceUpdatesHandler {
    private val handler = Handler()
    private val state: TrackingState  = TrackingState(context)
    private var hyperTrackMap: HyperTrackMap? = null
    private val connectivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            view.updateConnectionStatus(false)
            realTimeUpdatesService.subscribeToDeviceUpdates(
                hyperTrackService.deviceId,
                this@TrackingPresenter
            )
            hyperTrackService.syncDeviceSettings()
        }
    }
    private var tripInfoUpdater: Timer? = null

    init {
        this.context.registerReceiver(connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    fun subscribeUpdates(googleMap: GoogleMap) {
        if (hyperTrackMap == null) {
            val mapAdapter = GoogleMapAdapter(googleMap, getBuilder(context).build())
            mapAdapter.addTripFilter { trip -> trip.tripId == state.selectedTripId }
            hyperTrackMap = HyperTrackMap.getInstance(context, mapAdapter)
                .bind(GpsLocationProvider(context))
        }
        hyperTrackMap!!.bind(realTimeUpdatesService, hyperTrackService.deviceId)
        realTimeUpdatesService.subscribeToDeviceUpdates(hyperTrackService.deviceId, this)
        hyperTrackService.syncDeviceSettings()
        val selectedTrip = state.trips[state.selectedTripId]
        if (selectedTrip == null) {
            view.showSearch()
            hyperTrackMap!!.moveToMyLocation()
        } else {
            hyperTrackMap!!.moveToTrip(selectedTrip)
        }
        if (!state.isHomeLatLngAdded) {
            view.addSearchPlaceFragment(SearchPlaceFragment.Config.HOME_ADDRESS)
        }
    }

    fun pause() {
        hyperTrackMap!!.unbindHyperTrackViews()
        realTimeUpdatesService.unsubscribeFromDeviceUpdates(this)
    }

    fun setCameraFixedEnabled(enabled: Boolean) {
        if (hyperTrackMap != null) {
            if (enabled) {
                val selectedTrip = state.trips[state.selectedTripId]
                if (selectedTrip == null) {
                    view.showSearch()
                    hyperTrackMap!!.moveToMyLocation()
                } else {
                    hyperTrackMap!!.moveToTrip(selectedTrip)
                }
            }
            hyperTrackMap!!.adapter().setCameraFixedEnabled(enabled)
        }
    }

    fun openSearch() {
        view.addSearchPlaceFragment(SearchPlaceFragment.Config.SEARCH_PLACE)
    }

    fun selectTrip(trip: Trip?) {
        state.setSelectedTrip(trip)
        if (trip!!.status == "completed") {
            view.showTripSummaryInfo(trip)
        } else {
            view.showTripInfo(trip)
        }
        if (hyperTrackMap != null) {
            hyperTrackMap!!.adapter().notifyDataSetChanged()
            hyperTrackMap!!.moveToTrip(trip)
        }
    }

    fun endTrip() {
        if (!TextUtils.isEmpty(state.selectedTripId)) {
            view.showProgressBar()
            backendProvider.completeTrip(state.selectedTripId, object : ResultHandler<String> {
                override fun onResult(result: String) {
                    Log.d(TAG, "trip is ended: $result")
                    view.hideProgressBar()
                }

                override fun onError(error: Exception) {
                    Log.e(TAG, "complete trip failure", error)
                    view.hideProgressBar()
                    Toast.makeText(context, "Complete trip failure", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun startTripInfoUpdating(trip: Trip) {
        stopTripInfoUpdating()
        tripInfoUpdater = Timer()
        tripInfoUpdater!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post { view.showTripInfo(trip) }
            }
        }, 60000, 60000)
    }

    fun stopTripInfoUpdating() {
        if (tripInfoUpdater != null) {
            tripInfoUpdater!!.cancel()
            tripInfoUpdater = null
        }
    }

    fun destroy() {
        stopTripInfoUpdating()
        if (hyperTrackMap != null) {
            hyperTrackMap!!.destroy()
            hyperTrackMap = null
        }
        realTimeUpdatesService.unsubscribeFromDeviceUpdates(this)
        context.unregisterReceiver(connectivityReceiver)
    }

    override fun onLocationUpdateReceived(location: Location) {}
    override fun onBatteryStateUpdateReceived(i: Int) {}
    override fun onStatusUpdateReceived(statusUpdate: StatusUpdate) {
        val status: String
        status = when (statusUpdate.value) {
            StatusUpdate.STOPPED -> context.getString(R.string.status_stopped)
            else -> "unknown"
        }
        view.onStatusUpdateReceived(String.format(context.getString(R.string.tracking_is), status))
    }

    override fun onTripUpdateReceived(trip: Trip) {
        Log.d(TAG, "onTripUpdateReceived: $trip")
        if (hyperTrackMap != null) {
            state.trips[trip.tripId] = trip
            val isNewTrip = !state.tripSubscription.containsKey(trip.tripId)
            val isActive = trip.status != "completed"
            if (isActive) {
                if (isNewTrip) {
                    val tripSubscription = hyperTrackMap!!.subscribeTrip(trip.tripId)
                    state.tripSubscription[trip.tripId] = tripSubscription
                    hyperTrackMap!!.moveToTrip(trip)
                }
                if (trip.tripId == state.selectedTripId) {
                    view.showTripInfo(trip)
                }
            } else {
                state.trips.remove(trip.tripId)
                if (!isNewTrip) {
                    state.tripSubscription.remove(trip.tripId)!!.remove()
                }
            }
            val trips = state.allTripsStartingFromLatest
            var selectedTripIndex = 0
            if (!trips.isEmpty()) {
                var selectedTrip = state.trips[state.selectedTripId]
                if (selectedTrip == null) {
                    selectedTrip = trips[0]
                    selectTrip(selectedTrip)
                } else {
                    state.setSelectedTrip(selectedTrip)
                }
                selectedTripIndex = trips.indexOf(selectedTrip)
            } else {
                state.setSelectedTrip(null)
            }
            view.updateTripsMenu(trips, selectedTripIndex)
        }
    }

    override fun onError(e: Exception, s: String) {}
    override fun onCompleted(s: String) {}
    interface View {
        fun updateConnectionStatus(offline: Boolean)
        fun onStatusUpdateReceived(statusText: String)
        fun showSearch()
        fun updateTripsMenu(trips: List<Trip>, selectedTripIndex: Int)
        fun showTripInfo(trip: Trip)
        fun showTripSummaryInfo(trip: Trip)
        fun showProgressBar()
        fun hideProgressBar()
        fun addSearchPlaceFragment(config: SearchPlaceFragment.Config?)
    }

    fun shareTrackMessage() {
        val shareableMessage = state.getShareMessage()
        if (shareableMessage.isEmpty()) return
        shareAction(context, shareableMessage)
    }
    companion object {
        private const val TAG = "TrackingPresenter"
        const val SHARE_BROADCAST_ACTION = "com.hypertrack.logistics.SHARE_TRIP"

        @JvmStatic
        fun shareAction(context: Context, shareableMessage: String?) {
            val sharingTitle: String = context.getString(R.string.share_trip_via)
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareableMessage)
            sendIntent.type = "text/plain"
            val intent = Intent(SHARE_BROADCAST_ACTION)
            intent.setPackage(context.packageName)
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val chooser = Intent.createChooser(sendIntent, sharingTitle, pendingIntent.intentSender)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

}