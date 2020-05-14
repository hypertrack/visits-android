package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.DeliveriesRepository
import com.hypertrack.android.utils.getServiceLocator
import kotlinx.coroutines.launch

class ListActivityViewModel(application: Application) : AndroidViewModel(application) {


    private val deliveriesRepository: DeliveriesRepository = application.getServiceLocator().getDeliveriesRepo()

    val deliveries = deliveriesRepository.deliveryListItems

    init {
        viewModelScope.launch {
            deliveriesRepository.refreshDeliveries()
        }
    }



}

