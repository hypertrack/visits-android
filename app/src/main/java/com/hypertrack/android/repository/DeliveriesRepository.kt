package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.utils.DeliveriesStorage
import com.hypertrack.android.utils.OsUtilsProvider
import java.lang.IllegalArgumentException

const val COMPLETED = "Completed"

const val VISITED = "Visited"

const val PENDING = "Pending"

class DeliveriesRepository(
    private val osUtilsProvider: OsUtilsProvider,
    private val apiClient: ApiClient,
    private val deliveriesStorage: DeliveriesStorage
) {

    private val _deliveriesMap: MutableMap<String, Delivery>
            = deliveriesStorage.restoreDeliveries().associateBy { it._id  }.toMutableMap()

    private val _deliveryListItems: MutableLiveData<List<DeliveryListItem>>
            = MutableLiveData(_deliveriesMap.values.sortedWithHeaders())

    private val _deliveryItemsById: Map<String, MutableLiveData<Delivery>>
            = _deliveriesMap.mapValues { MutableLiveData(it.value) }

    val deliveryListItems: LiveData<List<DeliveryListItem>>
        get() = _deliveryListItems

    suspend fun refreshDeliveries() {

        val geofences = apiClient.getGeofences()
        Log.d(TAG, "Got geofences $geofences")
        // if delivery object is already present -> update visited & metadata state. if updated, then post new value in _deliveryItemsById
        // else - create new delivery and add it to _deliveryItemsById
        // post updated deliveryListItems
        geofences.forEach { geofence ->
            val currentValue = _deliveriesMap[geofence.geofence_id]
            if (currentValue == null) {
                _deliveriesMap[geofence.geofence_id] = Delivery(geofence, osUtilsProvider)
            } else {
                val newValue = currentValue.update(geofence)
                _deliveriesMap[geofence.geofence_id] = newValue
                _deliveryItemsById[geofence.geofence_id]?.postValue(newValue)
            }
        }

        deliveriesStorage.saveDeliveries(_deliveriesMap.values.toList())
        _deliveryListItems.postValue(_deliveriesMap.values.sortedWithHeaders())
    }

    fun deliveryForId(id: String): LiveData<Delivery> {
        return _deliveryItemsById[id]?: throw IllegalArgumentException("No delivery for id $id")
    }

    fun updateDeliveryNote(id: String, newNote: String) {
        Log.d(TAG, "Updating delivery $id with note $newNote")
        val target = _deliveriesMap[id] ?: throw IllegalArgumentException("No delivery for id $id")
        // Brake infinite cycle
        if (target.deliveryNote == newNote) return

        val updatedNote = target.updateNote(newNote)
        _deliveriesMap[id] = updatedNote
        deliveriesStorage.saveDeliveries(_deliveriesMap.values.toList())
        _deliveryItemsById[id]?.postValue(updatedNote)
        _deliveryListItems.postValue(_deliveriesMap.values.sortedWithHeaders())
    }

    fun markCompleted(id: String) {
        val target = _deliveriesMap[id] ?: throw IllegalArgumentException("No delivery for id $id")
        if (target.isCompleted) return
        val completedDelivery = target.complete(osUtilsProvider.getCurrentTimestamp())
        _deliveriesMap[id] = completedDelivery
        deliveriesStorage.saveDeliveries(_deliveriesMap.values.toList())
        _deliveryItemsById[id]?.postValue(completedDelivery)
        _deliveryListItems.postValue(_deliveriesMap.values.sortedWithHeaders())
    }


    companion object { const val TAG = "DeliveriesRepository"}


}

private fun Collection<Delivery>.sortedWithHeaders(): List<DeliveryListItem> {
    val grouped = this.groupBy { it.status }
    val result = ArrayList<DeliveryListItem>(this.size + grouped.keys.size)
    grouped.keys.forEach { deliveryType ->
        result.add(HeaderDeliveryItem(deliveryType))
        result.addAll(grouped[deliveryType] ?: emptyList())
    }
    return result
}

sealed class DeliveryListItem
data class HeaderDeliveryItem(val text : String) : DeliveryListItem()
data class Delivery(val _id : String,
                    val delivery_id : String = "", val driver_id : String = "", val customerNote : String = "",
                    val createdAt : String = "", val updatedAt : String = "",
                    val address : Address = Address(
        "",
        "",
        "",
        ""
    ),
                    val deliveryNote : String = "", var deliveryPicture : String = "",
                    var enteredAt :String = "",
                    val completedAt : String = "", val exitedAt : String = "",
                    val latitude : Double = 47.839042, val longitude: Double = 35.101726) : DeliveryListItem() {
    val isCompleted: Boolean
        get() = status == COMPLETED

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

    fun update(geofence: Geofence) : Delivery {
        return this // TODO Denys - update when API adds support to geofence events
//        val pass: Unit = Unit
//        when {
//            (geofence.entered_at != enteredAt) -> pass
//            (geofence.exited_at != exitedAt) -> pass
//            (geofence.metadata.toString() != customerNote) -> pass
//            else -> return this
//        }
//        return Delivery(_id, delivery_id, driver_id, geofence.metadata.toString(),
//        createdAt, updatedAt, address, deliveryNote, deliveryPicture, geofence.entered_at,
//            completedAt, geofence.exited_at, latitude, longitude)

    }

    fun updateNote(newNote: String): Delivery {
        return Delivery(_id, delivery_id, driver_id, customerNote,
        createdAt, updatedAt, address, newNote, deliveryPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude)
    }

    fun complete(completedAt: String): Delivery {
        return Delivery(_id, delivery_id, driver_id, customerNote,
            createdAt, updatedAt, address, deliveryNote, deliveryPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude)
    }

    constructor(geofence: Geofence, osUtilsProvider: OsUtilsProvider) : this(
        _id = geofence.geofence_id,
        customerNote = geofence.metadata.toString(),
        address = osUtilsProvider.getAddressFromCoordinates(geofence.latitude, geofence.longitude),
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
    latitude = geofence.latitude, longitude = geofence.longitude)
}


data class Address (val street : String, val postalCode : String, val city : String, val country : String)