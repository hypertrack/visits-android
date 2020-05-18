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


    fun onDeliveryNoteChanged(newNote : String) {
        Log.d(TAG, "onDeliveryNoteChanged $newNote")
        deliveriesRepository.updateDeliveryNote(id, newNote)
    }

    fun onMarkedCompleted() = deliveriesRepository.markCompleted(id)
    fun getLatLng(): LatLng {
        return LatLng(delivery.value!!.latitude, delivery.value!!.longitude)
    }
    fun getLabel() : String {
        return "Parcel ${delivery.value?._id}"
    }

    companion object {const val TAG = "DeliveryStatusVM"}
}