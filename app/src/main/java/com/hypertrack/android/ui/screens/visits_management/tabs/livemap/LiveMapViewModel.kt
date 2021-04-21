package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.Dispatchers
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

    private val _state: MutableLiveData<LiveMapState> = MutableLiveData(Paused)
    val state: LiveData<LiveMapState>
      get() = _state

    fun mapLoading() = _state.postValue(Loading)

    suspend fun getMap(): GoogleMap = suspendCoroutine { continuation ->
        viewModelScope.launch(Dispatchers.Default) {
            mapLock.withLock {
                _googleMap
                    ?.let { viewModelScope.launch(Dispatchers.Main) { continuation.resume(it) } }
                    ?: callbacks.add(continuation)
            }
        }
    }

    fun onHomeAddressClicked() {
        _state.postValue(SetHome(_googleMap!!))
    }

    fun onSearchPlaceSelected() {
        _state.postValue(SearchPlace(_googleMap!!))
    }

    fun onPlaceSelected() {
        _state.postValue(OnTrip(_googleMap!!))
    }

    var googleMap: GoogleMap?
        get() = _googleMap
        set(value) {
            if (value != null) _state.postValue(OnTrip(value)) else _state.postValue(Error)
            viewModelScope.launch(Dispatchers.Default) {
                mapLock.withLock {
                    _googleMap = value
                    value?.let {
                        callbacks.forEach { viewModelScope.launch(Dispatchers.Main) { it.resume(value) } }
                        callbacks.clear()
                    }

                }
            }
        }

    companion object {const val TAG = "LiveMapVM"}
}

sealed class LiveMapState
object Paused : LiveMapState()
object Loading : LiveMapState()
object Error : LiveMapState()
class OnTrip(val map: GoogleMap) : LiveMapState()
class SearchPlace(val map: GoogleMap) : LiveMapState()
class SetHome(val map: GoogleMap) : LiveMapState()
