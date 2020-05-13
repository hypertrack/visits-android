package com.hypertrack.android.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.repository.DeliveriesRepository
import com.hypertrack.android.response.Delivery
import com.hypertrack.android.utils.getServiceLocator

class ListActivityViewModel(application: Application) : AndroidViewModel(application) {


    private val deliveriesRepository: DeliveriesRepository = application.getServiceLocator().getDeliveriesRepo()

    private val _deliveries = MutableLiveData<List<Delivery>>(emptyList())

    val deliveries: LiveData<List<Delivery>>
        get() = _deliveries



}
