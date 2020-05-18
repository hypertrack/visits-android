package com.hypertrack.android.models

import android.content.Context
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.TrackingError
import com.hypertrack.sdk.TrackingStateObserver

class HyperTrackService(publishableKey: String, context: Context) {
    private val listener = TrackingState()
    private val instance = HyperTrack.getInstance(context, publishableKey).addTrackingListener(listener)

    init {
        when(instance.isRunning) {
            true -> listener.onTrackingStart()
            else -> listener.onTrackingStop()
        }
    }

    var driverId: String
    get() = throw NotImplementedError()
    set(value) {
        instance.setDeviceMetadata(mapOf("driver_id" to value))
    }

    val deviceId: String
        get() = instance.deviceID

    val state: TrackingStateValue
        get() = listener.state
}

private class TrackingState : TrackingStateObserver.OnTrackingStateChangeListener {
    var state: TrackingStateValue = TrackingStateValue.UNKNOWN
    override fun onTrackingStart() { state = TrackingStateValue.TRACKING }

    override fun onError(p0: TrackingError?) {
        state = TrackingStateValue.ERROR
    }

    override fun onTrackingStop() {
        state = TrackingStateValue.STOP
    }

}

enum class TrackingStateValue {
    TRACKING, ERROR, STOP, UNKNOWN
}