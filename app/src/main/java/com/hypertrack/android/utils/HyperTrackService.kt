package com.hypertrack.android.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.TrackingError
import com.hypertrack.sdk.TrackingStateObserver

class HyperTrackService(private val listener: TrackingState, private val sdkInstance: HyperTrack) {


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

    fun sendCompletionEvent(
        id: String,
        visitNote: String,
        typeKey: String,
        isCompleted: Boolean
    ) {
        val completionStatus = if (isCompleted) "CHECK_OUT" else "CANCEL"
        val payload = mapOf(typeKey to id, "type" to completionStatus, "delivery_note" to visitNote)
        Log.d(TAG, "Completion event payload $payload")
        sdkInstance.addTripMarker(payload)
    }

    fun createVisitStartEvent(id: String, typeKey: String) {
        sdkInstance.addTripMarker(mapOf(typeKey to id, "type" to "CHECK_IN"))
    }

    fun sendPickedUp(id: String, typeKey: String) {
        sdkInstance.addTripMarker(mapOf(typeKey to id, "type" to "PICK_UP"))
    }

    fun clockOut() {
        sdkInstance.addTripMarker(mapOf("type" to "CLOCK_OUT"))
        sdkInstance.stop()
    }

    fun clockIn() {
        sdkInstance.start()
        sdkInstance.addTripMarker(mapOf("type" to "CLOCK_IN"))
    }

    companion object {
        private const val TAG = "HyperTrackAdapter"
    }
}


class TrackingState : TrackingStateObserver.OnTrackingStateChangeListener {
    var state: MutableLiveData<TrackingStateValue> =
        MutableLiveData(TrackingStateValue.UNKNOWN)

    override fun onTrackingStart() = state.postValue(TrackingStateValue.TRACKING)

    override fun onError(p0: TrackingError?) = state.postValue(TrackingStateValue.ERROR)

    override fun onTrackingStop() = state.postValue(TrackingStateValue.STOP)

}

enum class TrackingStateValue {
    TRACKING, ERROR, STOP, UNKNOWN
}