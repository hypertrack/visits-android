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
    var deliveryNote = delivery.value?.deliveryNote?:""


    fun onDeliveryNoteChanged(newNote : String) {
        Log.d(TAG, "onDeliveryNoteChanged $newNote")
        deliveryNote = newNote
    }

    fun onMarkedCompleted() = deliveriesRepository.markCompleted(id)

    fun getLatLng(): LatLng = LatLng(delivery.value!!.latitude, delivery.value!!.longitude)

    fun getLabel() : String = "Parcel ${delivery.value?._id}"

    fun onBackPressed() = deliveriesRepository.updateDeliveryNote(id, deliveryNote)

    companion object {const val TAG = "DeliveryStatusVM"}
}