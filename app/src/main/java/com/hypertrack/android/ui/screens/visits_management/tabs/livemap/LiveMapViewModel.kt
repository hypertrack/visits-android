package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LiveMapViewModel : ViewModel() {

    private var callbacks: MutableSet<Continuation<GoogleMap>> = mutableSetOf()

    suspend fun getMap(): GoogleMap = suspendCoroutine { continuation ->
        _googleMap?.let {
            continuation.resume(it)
            return@suspendCoroutine
        }
        callbacks.add(continuation)
    }

    private var _googleMap:GoogleMap? = null
    var googleMap: GoogleMap?
        get() = _googleMap
        set(value) {
            _googleMap = value
            value?.let {
                callbacks.forEach { it.resume(value) }
                callbacks.clear()
            }
        }
}