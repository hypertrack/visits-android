package com.hypertrack.android.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.DeliveriesRepository
import kotlinx.coroutines.launch

class DeliveryListViewModel(private val deliveriesRepository: DeliveriesRepository) : ViewModel() {

    val deliveries = deliveriesRepository.deliveryListItems

    val trackingState = deliveriesRepository.trackingState

    init {
        viewModelScope.launch {
            deliveriesRepository.refreshDeliveries()
        }
    }

}

