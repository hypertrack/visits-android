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
    private var _googleMap:GoogleMap? = null
    private var callbacks: MutableSet<Continuation<GoogleMap>> = CopyOnWriteArraySet()

    suspend fun getMap(): GoogleMap = suspendCoroutine { continuation ->
        GlobalScope.launch(Dispatchers.Default) {
            mapLock.withLock {
                _googleMap
                    ?.let { GlobalScope.launch(Dispatchers.Main) { continuation.resume(it) } }
                    ?: callbacks.add(continuation)
            }
        }
    }

    var googleMap: GoogleMap?
        get() = _googleMap
        set(value) {
            GlobalScope.launch(Dispatchers.Default) {
                mapLock.withLock {
                    _googleMap = value
                    value?.let {
                        callbacks.forEach { GlobalScope.launch(Dispatchers.Main) { it.resume(value) } }
                        callbacks.clear()
                    }

                }
            }
        }

}