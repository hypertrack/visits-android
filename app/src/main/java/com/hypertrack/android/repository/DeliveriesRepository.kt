package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.response.Delivery
import com.hypertrack.android.utils.DeliveriesStorage

class DeliveriesRepository(private val apiClient: ApiClient, private val deliveriesStorage : DeliveriesStorage) {

    private val _deliveries: MutableLiveData<List<Delivery>> = MutableLiveData(deliveriesStorage.restoreDeliveries())

    val deliveries: LiveData<List<Delivery>>
        get() = _deliveries

    suspend fun refreshDeliveries() {

        val geofences = apiClient.getGeofences()
        Log.d(TAG, "Got geofences $geofences")
        val existingIds = deliveries.value?.map { it._id }?.toSet() ?: emptySet()
        val newDeliveries = toDeliveries(geofences.filter { !existingIds.contains(it.geofence_id) })
        deliveriesStorage.saveDeliveries(newDeliveries)
        _deliveries.postValue(newDeliveries)
    }

    private fun toDeliveries(geofences: List<Geofence>) : List<Delivery> {
        // TODO Add address from geocoder
        return geofences.map { geofence ->
            Delivery(
                status = "Pending", _id = geofence.geofence_id,
                createdAt = geofence.created_at,
                latitude = geofence.latitude, longitude = geofence.longitude
            )
        }
    }


    companion object { const val TAG = "DeliveriesRepository"}


}
