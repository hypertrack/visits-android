package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.view_models.Address
import com.hypertrack.android.view_models.Delivery
import com.hypertrack.android.utils.DeliveriesStorage
import com.hypertrack.android.view_models.DeliveryListItem

class DeliveriesRepository(private val apiClient: ApiClient, private val deliveriesStorage : DeliveriesStorage) {

    private val _deliveryLlistItems: MutableLiveData<List<DeliveryListItem>> = MutableLiveData(deliveriesStorage.restoreDeliveries())

    val deliveryListItems: LiveData<List<DeliveryListItem>>
        get() = _deliveryLlistItems

    suspend fun refreshDeliveries() {

        val geofences = apiClient.getGeofences()
        Log.d(TAG, "Got geofences $geofences")
        val existingIds = deliveryListItems.value?.filterIsInstance<Delivery>()?.map { it._id }?.toSet() ?: emptySet()
        val newDeliveries = toDeliveries(geofences.filter { !existingIds.contains(it.geofence_id) })
        deliveriesStorage.saveDeliveries(newDeliveries)
        _deliveryLlistItems.postValue(newDeliveries)
    }

    private fun toDeliveries(geofences: List<Geofence>) : List<Delivery> {
        return geofences.map { geofence ->
            val address = Address(
                "street ${geofence.geofence_id.substringBefore("-")}",
                "HOHOHO", "Zaporozhzhye", "Ukraine"
            )
            Delivery(
                status = "Pending", _id = geofence.geofence_id,
                createdAt = geofence.created_at, address = address,
                latitude = geofence.latitude, longitude = geofence.longitude
            )
        }
    }


    companion object { const val TAG = "DeliveriesRepository"}


}
