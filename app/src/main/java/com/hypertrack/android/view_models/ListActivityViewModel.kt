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

sealed class DeliveryListItem

data class HeaderDeliveryItem(val text : String) : DeliveryListItem()
data class Delivery(val status : String, val _id : String,
                    val delivery_id : String = "", val driver_id : String = "",
                    val label : String = "", val customerNote : String = "",
                    val createdAt : String = "", val updatedAt : String = "",
                    val items : List<Items> = emptyList(), val address : Address = Address("", "", "", ""),
                    val deliveryNote : String = "", var deliveryPicture : String = "", var enteredAt :String = "",
                    val completedAt : String = "", val exitedAt : String = "",
                    val latitude : Double? = null, val longitude: Double? = null) : DeliveryListItem()

data class Items(val _id : String, val item_id :String, val item_label : String, val item_sku : String)
data class Address (val street : String, val postalCode : String, val city : String, val country : String)