package com.hypertrack.android.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.ServiceNotificationConfig
import com.hypertrack.sdk.TrackingError
import com.hypertrack.sdk.TrackingStateObserver

class HyperTrackService(publishableKey: String, context: Context) {

    private val listener = TrackingState()
    private val sdkInstance = HyperTrack
        .getInstance(context, publishableKey)
        .addTrackingListener(listener)
        .setTrackingNotificationConfig(
            ServiceNotificationConfig.Builder()
                .setSmallIcon(R.drawable.ic_logo_small)
                .build()
        )
        .allowMockLocations()

    init {
        when(sdkInstance.isRunning) {
            true -> listener.onTrackingStart()
            else -> listener.onTrackingStop()
        }
    }

    var driverId: String
    get() = throw NotImplementedError()
    set(value) {
        sdkInstance.setDeviceMetadata(mapOf("driver_id" to value))
    }

    val deviceId: String
        get() = sdkInstance.deviceID

    val state: LiveData<TrackingStateValue>
        get() = listener.state

    fun sendUpdatedNote(id: String, newNote: String) {
        sdkInstance.addTripMarker(mapOf(GEOFENCE_ID to id, "delivery_note" to newNote))
    }

    fun sendCompletionEvent(id: String) {
        sdkInstance.addTripMarker(mapOf(GEOFENCE_ID to id, "completed" to true))
    }

    fun createVisitStartEvent(id: String) {
        sdkInstance.addTripMarker(mapOf(GEOFENCE_ID to id, "created" to true))
    }

    companion object { private const val GEOFENCE_ID = "geofence_id" }
}

private class TrackingState : TrackingStateObserver.OnTrackingStateChangeListener {
    var state: MutableLiveData<TrackingStateValue> =
        MutableLiveData(TrackingStateValue.UNKNOWN)

    override fun onTrackingStart() = state.postValue(TrackingStateValue.TRACKING)

    override fun onError(p0: TrackingError?) = state.postValue(TrackingStateValue.ERROR)

    override fun onTrackingStop() = state.postValue(TrackingStateValue.STOP)

}

enum class TrackingStateValue {
    TRACKING, ERROR, STOP, UNKNOWN
}