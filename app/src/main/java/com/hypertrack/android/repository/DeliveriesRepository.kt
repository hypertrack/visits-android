package com.hypertrack.android.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.getAddressFromCoordinates
import com.hypertrack.android.utils.DeliveriesStorage

const val COMPLETED = "Completed"

const val VISITED = "Visited"

const val PENDING = "Pending"

class DeliveriesRepository(
    private val context: Context,
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
        deliveriesStorage.saveDeliveries(newDeliveries.filterIsInstance<Delivery>())
        _deliveryListItems.postValue(newDeliveries)
    }

    private fun toDeliveries(geofences: List<Geofence>, currentItems: Map<String, Delivery>) : List<DeliveryListItem> {
        val deliveries = geofences.map { geofence ->
            currentItems[geofence.geofence_id]
                ?: Delivery(_id = geofence.geofence_id,
                createdAt = geofence.created_at,
                    address = context.getAddressFromCoordinates(geofence.latitude, geofence.longitude),
                latitude = geofence.latitude, longitude = geofence.longitude
            )
        }
        Log.d(TAG, "Updated deliveries $deliveries")
        // Add headers
        val groupped = deliveries.groupBy { it.status }
        val result = mutableListOf<DeliveryListItem>()
        for (type in listOf(PENDING, VISITED, COMPLETED)) {
            groupped[type]?.let {
                if (it.isNotEmpty()) {
                    result.add(HeaderDeliveryItem(type))
                    result.addAll(it)
                }
            }

        }


        return result
    }


    companion object { const val TAG = "DeliveriesRepository"}


}

sealed class DeliveryListItem
data class HeaderDeliveryItem(val text : String) : DeliveryListItem()
data class Delivery(val _id : String,
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
                    val latitude : Double? = null, val longitude: Double? = null) : DeliveryListItem() {
    val status: String
    get() {
        return when {
            completedAt.isNotEmpty() -> COMPLETED
            enteredAt.isNotEmpty() -> VISITED
            else -> PENDING
        }
    }
    fun hasPicture() = deliveryPicture.isNotEmpty()
    fun hasNotes() = deliveryNote.isNotEmpty()
}

data class Items(val _id : String, val item_id :String, val item_label : String, val item_sku : String)
data class Address (val street : String, val postalCode : String, val city : String, val country : String)