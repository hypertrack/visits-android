package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.VisitsRepository
import kotlinx.coroutines.launch

class VisitsManagementViewModel(private val visitsRepository: VisitsRepository) : ViewModel() {
    val clockinButtonText: LiveData<CharSequence> = TODO("Not yet implemented")
    val checkinButtonText: LiveData<CharSequence> = TODO("Not yet implemented")

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

