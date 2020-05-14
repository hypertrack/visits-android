package com.hypertrack.android.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.getAddressFromCoordinates
import com.hypertrack.android.utils.DeliveriesStorage

class DeliveriesRepository(
    val context: Context,
    private val apiClient: ApiClient,
    private val deliveriesStorage: DeliveriesStorage
) {

    private val _deliveryListItems: MutableLiveData<List<DeliveryListItem>> = MutableLiveData(deliveriesStorage.restoreDeliveries())

    val deliveryListItems: LiveData<List<DeliveryListItem>>
        get() = _deliveryListItems

    suspend fun refreshDeliveries() {

        val geofences = apiClient.getGeofences()
        Log.d(TAG, "Got geofences $geofences")
        val currentItems = deliveryListItems.value?.filterIsInstance<Delivery>()?.associateBy { it._id } ?: emptyMap()
        val newDeliveries = toDeliveries(geofences, currentItems)
        _deliveryListItems.postValue(newDeliveries)
    }

    private fun toDeliveries(geofences: List<Geofence>, currentItems: Map<String, Delivery>) : List<DeliveryListItem> {
        val deliveries = geofences.map { geofence ->
            currentItems[geofence.geofence_id]
                ?: Delivery(
                status = "Pending", _id = geofence.geofence_id,
                createdAt = geofence.created_at,
                    address = context.getAddressFromCoordinates(geofence.latitude, geofence.longitude),
                latitude = geofence.latitude, longitude = geofence.longitude
            )
        }
        Log.d(TAG, "Updated deliveries $deliveries")
        // Add headers
        val completed = deliveries.filter { it.completedAt.isNotEmpty() }
        val visited = deliveries.filter { it.enteredAt.isNotEmpty() }
        val pending = deliveries.filter { !(visited.contains(it) || completed.contains(it)) }
        val result = mutableListOf<DeliveryListItem>()
        if (pending.isNotEmpty()) {
            result.add(HeaderDeliveryItem("Pending"))
            result.addAll(pending)
        }
        if (visited.isNotEmpty()) {
            result.add(HeaderDeliveryItem("Visited"))
            result.addAll(visited)
        }
        if (completed.isNotEmpty()) {
            result.add(HeaderDeliveryItem("Completed"))
            result.addAll(completed)
        }

        return result
    }


    companion object { const val TAG = "DeliveriesRepository"}


}

sealed class DeliveryListItem
data class HeaderDeliveryItem(val text : String) : DeliveryListItem()
data class Delivery(val status : String, val _id : String,
                    val delivery_id : String = "", val driver_id : String = "",
                    val label : String = "", val customerNote : String = "",
                    val createdAt : String = "", val updatedAt : String = "",
                    val items : List<Items> = emptyList(), val address : Address = Address(
        "",
        "",
        "",
        ""
    ),
                    val deliveryNote : String = "", var deliveryPicture : String = "",
                    var enteredAt :String = "",
                    val completedAt : String = "", val exitedAt : String = "",
                    val latitude : Double? = null, val longitude: Double? = null) : DeliveryListItem()

data class Items(val _id : String, val item_id :String, val item_label : String, val item_sku : String)
data class Address (val street : String, val postalCode : String, val city : String, val country : String)