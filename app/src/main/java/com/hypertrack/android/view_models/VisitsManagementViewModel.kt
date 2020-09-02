package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.*
import com.hypertrack.android.repository.VisitsRepository
import kotlinx.coroutines.launch

class VisitsManagementViewModel(private val visitsRepository: VisitsRepository) : ViewModel() {

    private val _clockinButtonText = MediatorLiveData<CharSequence>()
    init {
        _clockinButtonText.addSource(visitsRepository.isTracking) { tracking ->
            _clockinButtonText.postValue(if (tracking) "Clock Out" else "Clock In")
        }
    }
    val clockinButtonText: LiveData<CharSequence>
        get() = _clockinButtonText

    private val _checkinButtonText = MediatorLiveData<CharSequence>()
    init {
        _checkinButtonText.addSource(visitsRepository.hasOngoingLocalVisit) { hasVisit ->
            _checkinButtonText.postValue(if (hasVisit) "CheckOut" else "CheckIn")
        }
    }
    val checkinButtonText: LiveData<CharSequence>
        get() = _checkinButtonText

    private val _showSpinner = MutableLiveData(false)
    val showSpinner: LiveData<Boolean>
        get() = _showSpinner

    private val _enableCheckin = MutableLiveData(false)
    val enableCheckin: LiveData<Boolean>
        get() = _enableCheckin

    fun refreshVisits() {
        _showSpinner.postValue(true)
        viewModelScope.launch {
            visitsRepository.refreshVisits()
            _showSpinner.postValue(false)
        }
    }

    fun switchTracking() {
        Log.v(TAG, "switchTracking")
        _showSpinner.postValue(true)
        viewModelScope.launch {
            visitsRepository.switchTracking()
            _showSpinner.postValue(false)

        }
    }

    fun checkin() {
        Log.v(TAG, "checkin")
        visitsRepository.processLocalVisit()
    }

    val visits = visitsRepository.visitListItems

    val statusLabel = visitsRepository.statusLabel


    init {
        viewModelScope.launch {
            visitsRepository.refreshVisits()
        }
    }

    companion object {
        const val TAG = "VisitsManagementVM"
    }

}

