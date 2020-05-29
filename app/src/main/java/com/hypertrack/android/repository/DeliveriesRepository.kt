package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.DeliveriesStorage
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TrackingStateValue
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

const val COMPLETED = "Completed"
const val VISITED = "Visited"
const val PENDING = "Pending"

class DeliveriesRepository(
    private val osUtilsProvider: OsUtilsProvider,
    private val apiClient: ApiClient,
    private val deliveriesStorage: DeliveriesStorage,
    private val hyperTrackService: HyperTrackService
) {

    private val _deliveriesMap: MutableMap<String, Delivery>
            = deliveriesStorage.restoreDeliveries().associateBy { it._id  }.toMutableMap()

    private val _deliveryListItems: MutableLiveData<List<DeliveryListItem>>
            = MutableLiveData(_deliveriesMap.values.sortedWithHeaders())

    private val _deliveryItemsById: MutableMap<String, MutableLiveData<Delivery>>
            = _deliveriesMap.mapValues { MutableLiveData(it.value) }.toMutableMap()

    val deliveryListItems: LiveData<List<DeliveryListItem>>
        get() = _deliveryListItems

    private val _status = MediatorLiveData<Pair<TrackingStateValue, String>>()

    init{
        _status.addSource(hyperTrackService.state, Observer { state ->
            val label = _status.value?.second?:""
            _status.postValue(state to label)
        } )
        _status.addSource(deliveryListItems, Observer { items ->
            val trackingState = _status.value?.first?:TrackingStateValue.UNKNOWN
            val label = items.toStatusLabel()
            val fineLabel = if (label.isNotEmpty()) label else "No assigned deliveries"
            _status.postValue(trackingState to fineLabel)
        })
    }

    val statusLabel: LiveData<Pair<TrackingStateValue, String>>
        get() = _status

    suspend fun refreshDeliveries() {

        apiClient.getGeofences().forEach { geofence ->
            Log.d(TAG, "Processing geofence $geofence")
            val currentValue = _deliveriesMap[geofence.geofence_id]
            if (currentValue == null) {
                val delivery = Delivery(geofence, osUtilsProvider)
                _deliveriesMap[delivery._id] = delivery
                _deliveryItemsById[delivery._id] = MutableLiveData(delivery)
            } else {
                val newValue = currentValue.update(geofence)
                _deliveriesMap[geofence.geofence_id] = newValue
                // getValue/postValue invocations below are called on different instances:
                // `getValue` is called on Map with default value
                // while `postValue` is for MutableLiveData
                _deliveryItemsById[geofence.geofence_id]?.postValue(newValue) // updates MutableLiveData
            }
        }
        Log.d(TAG, "Updated _deliveriesMap $_deliveriesMap")
        Log.d(TAG, "Updated _deliveryItemsById $_deliveryItemsById")

        deliveriesStorage.saveDeliveries(_deliveriesMap.values.toList())
        _deliveryListItems.postValue(_deliveriesMap.values.sortedWithHeaders())
        Log.d(TAG, "Updated _deliveryListItems $_deliveryListItems")
    }

    fun deliveryForId(id: String): LiveData<Delivery> {
           return _deliveryItemsById[id]?:throw IllegalArgumentException("No delivery for id $id")
        }

    fun updateDeliveryNote(id: String, newNote: String): Boolean {
        Log.d(TAG, "Updating delivery $id with note $newNote")
        val target = _deliveriesMap[id] ?: throw IllegalArgumentException("No delivery for id $id")
        // Brake infinite cycle
        if (target.deliveryNote == newNote) return false

        val updatedNote = target.updateNote(newNote)
        _deliveriesMap[id] = updatedNote
        hyperTrackService.sendUpdatedNote(id, newNote)
        deliveriesStorage.saveDeliveries(_deliveriesMap.values.toList())
        _deliveryItemsById[id]?.postValue(updatedNote)
        _deliveryListItems.postValue(_deliveriesMap.values.sortedWithHeaders())
        return true
    }

    fun markCompleted(id: String) {
        val target = _deliveriesMap[id] ?: throw IllegalArgumentException("No delivery for id $id")
        if (target.isCompleted) return
        val completedDelivery = target.complete(osUtilsProvider.getCurrentTimestamp())
        _deliveriesMap[id] = completedDelivery
        hyperTrackService.sendCompletionEvent(id)
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

private fun List<DeliveryListItem>.toStatusLabel(): String {
    return filterIsInstance<Delivery>()
        .groupBy { it.status }
        .entries.
        fold("")
        {acc, entry -> acc + "${entry.value.size} ${entry.key} Item${if (entry.value.size == 1) " " else "s "}"}
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

        return if (toNote(geofence.metadata) == customerNote) this
            else Delivery(
                _id, delivery_id, driver_id, toNote(geofence.metadata),
                createdAt, updatedAt, address, deliveryNote, deliveryPicture, enteredAt,
                completedAt, exitedAt, latitude, longitude
            )
        // TODO Denys - update when API adds support to geofence events
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
        customerNote = toNote(geofence.metadata),
        address = osUtilsProvider.getAddressFromCoordinates(geofence.latitude, geofence.longitude),
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
    latitude = geofence.latitude, longitude = geofence.longitude)

}

private fun toNote(metadata: Map<String, Any>?): String {
    if (metadata == null) return ""
    val result = StringBuilder()
    metadata.forEach { (key, value) -> result.append("$key: $value\n") }
    return result.toString().dropLast(1)
}


data class Address (val street : String, val postalCode : String, val city : String, val country : String)