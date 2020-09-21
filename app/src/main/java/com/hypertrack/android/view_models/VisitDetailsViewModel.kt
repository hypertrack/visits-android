package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.models.Visit

class VisitDetailsViewModel(
    private val visitsRepository: VisitsRepository,
    private val id: String
) : ViewModel() {

    val visit: LiveData<Visit> = visitsRepository.visitForId(id)
    private var visitNote = visit.value?.visitNote?:""

    private val _showNoteUpdatedToast = MutableLiveData(false)

    val showNoteUpdatedToast: LiveData<Boolean>
        get() = _showNoteUpdatedToast

    private val _isEditable = MediatorLiveData<Boolean>()
    init {
        _isEditable.addSource(visitsRepository.isTracking, _isEditable::postValue)
    }
    val isEditable: LiveData<Boolean>
        get() = _isEditable

    fun onVisitNoteChanged(newNote : String) {
        Log.d(TAG, "onVisitNoteChanged $newNote")
        visitNote = newNote
    }

    fun onMarkedCompleted(isCompleted: Boolean) {
        _showNoteUpdatedToast.postValue(visitsRepository.updateVisitNote(id, visitNote))
        visitsRepository.markCompleted(id, isCompleted)
    }

    fun onPickupClicked() {
        val wasCancelled = visit.value?.tripVisitPickedUp ?: false
        if (wasCancelled) {
            onMarkedCompleted(false)
        } else {
            _showNoteUpdatedToast.postValue(visitsRepository.updateVisitNote(id, visitNote))
            visitsRepository.setPickedUp(id)
        }
    }

    fun getLatLng(): LatLng?  {
        visit.value?.latitude?.let { lat -> visit.value?.longitude?.let { lng -> return LatLng(lat, lng) } }
        return null
    }

    fun getLabel() : String = "Parcel ${visit.value?._id?:"unknown"}"

    fun onBackPressed() {
        val noteChanged = visitsRepository.updateVisitNote(id, visitNote)
        _showNoteUpdatedToast.postValue(noteChanged)

    }

    companion object {const val TAG = "VisitDetailsVM"}
}