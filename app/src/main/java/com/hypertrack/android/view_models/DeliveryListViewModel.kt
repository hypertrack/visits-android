package com.hypertrack.android.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.DeliveriesRepository
import kotlinx.coroutines.launch

class DeliveryListViewModel(private val deliveriesRepository: DeliveriesRepository) : ViewModel() {
    private val _showSpinner = MutableLiveData<Boolean>(false)
    val showSpinner: LiveData<Boolean>
        get() = _showSpinner

    fun rerfeshDeliveries() {
        _showSpinner.postValue(true)
        viewModelScope.launch {
            deliveriesRepository.refreshDeliveries()
            _showSpinner.postValue(false)
        }
    }

    val deliveries = deliveriesRepository.deliveryListItems

    val trackingState = deliveriesRepository.trackingState

    init {
        viewModelScope.launch {
            deliveriesRepository.refreshDeliveries()
        }
    }

}

