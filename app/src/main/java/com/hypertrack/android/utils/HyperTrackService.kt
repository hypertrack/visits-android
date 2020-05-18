package com.hypertrack.android.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.TrackingError
import com.hypertrack.sdk.TrackingStateObserver

class HyperTrackService(publishableKey: String, context: Context) {

    private val listener = TrackingState()
    private val sdkInstnce = HyperTrack
        .getInstance(context, publishableKey)
        .addTrackingListener(listener)

    init {
        when(sdkInstnce.isRunning) {
            true -> listener.onTrackingStart()
            else -> listener.onTrackingStop()
        }
    }

    var driverId: String
    get() = throw NotImplementedError()
    set(value) {
        sdkInstnce.setDeviceMetadata(mapOf("driver_id" to value))
    }

    val deviceId: String
        get() = sdkInstnce.deviceID

    val state: LiveData<TrackingStateValue>
        get() = listener.state

    fun sendUpdatedNote(id: String, newNote: String) {
        sdkInstnce.addTripMarker(mapOf(GEOFENCE_ID to id, "delivery_note" to newNote))
    }

    fun sendCompletionEvent(id: String) {
        sdkInstnce.addTripMarker(mapOf(GEOFENCE_ID to id, "completed" to true))
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