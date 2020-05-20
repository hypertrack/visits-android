package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.repository.DeliveriesRepository
import com.hypertrack.android.repository.Delivery

class DeliveryStatusViewModel(
    private val deliveriesRepository: DeliveriesRepository,
    private val id: String
) : ViewModel() {

    val delivery: LiveData<Delivery> = deliveriesRepository.deliveryForId(id)
    private var deliveryNote = delivery.value?.deliveryNote?:""


    fun onDeliveryNoteChanged(newNote : String) {
        Log.d(TAG, "onDeliveryNoteChanged $newNote")
        deliveryNote = newNote
    }

    fun onMarkedCompleted() = deliveriesRepository.markCompleted(id)

    fun getLatLng(): LatLng = LatLng(
        delivery.value?.latitude?:37.79337833161658,
        delivery.value?.longitude?:-122.39470660686493
    )

    fun getLabel() : String = "Parcel ${delivery.value?._id?:"unknown"}"

    fun onBackPressed() = deliveriesRepository.updateDeliveryNote(id, deliveryNote)

    companion object {const val TAG = "DeliveryStatusVM"}
}