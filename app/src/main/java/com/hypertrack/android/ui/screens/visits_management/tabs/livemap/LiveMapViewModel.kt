package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LiveMapViewModel : ViewModel() {



    private val mapLock = Mutex()
    private var callbacks: MutableSet<Continuation<GoogleMap>> = CopyOnWriteArraySet()
    suspend fun getMap(): GoogleMap = suspendCoroutine { continuation ->
        Log.d(TAG, "Get Map on $this")
        GlobalScope.launch(Dispatchers.Default) {
            mapLock.withLock {
                _googleMap
                    ?.let {
                        Log.d(TAG, "Map already loaded")
                        GlobalScope.launch(Dispatchers.Main) { continuation.resume(it) }
                    }
                    ?: { Log.d(TAG, "Saving continuation to callbacks $continuation")
                        callbacks.add(continuation)
                        Unit
                    }()
            }
        }
    }

    private var _googleMap:GoogleMap? = null
    var googleMap: GoogleMap?
        get() = _googleMap
        set(value) {
            Log.d(TAG, "Received GoogleMap $value in $this")
            GlobalScope.launch(Dispatchers.Default) {
                mapLock.withLock {
                    _googleMap = value
                    value?.let {
                        Log.d(TAG, "Notifying callbacks $callbacks")
                        callbacks.forEach { GlobalScope.launch(Dispatchers.Main) { it.resume(value) } }
                        callbacks.clear()
                    }

                }
            }
        }

    companion object { const val TAG = "LiveMapViewModel"}
}