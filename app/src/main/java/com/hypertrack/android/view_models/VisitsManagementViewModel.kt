package com.hypertrack.android.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.VisitsRepository
import kotlinx.coroutines.launch

class VisitsManagementViewModel(private val visitsRepository: VisitsRepository) : ViewModel() {
    private val _showSpinner = MutableLiveData(false)
    val showSpinner: LiveData<Boolean>
        get() = _showSpinner

    fun refreshVisits() {
        _showSpinner.postValue(true)
        viewModelScope.launch {
            visitsRepository.refreshVisits()
            _showSpinner.postValue(false)
        }
    }

    val visits = visitsRepository.visitListItems

    val statusLabel = visitsRepository.statusLabel

    init {
        viewModelScope.launch {
            visitsRepository.refreshVisits()
        }
    }

}

