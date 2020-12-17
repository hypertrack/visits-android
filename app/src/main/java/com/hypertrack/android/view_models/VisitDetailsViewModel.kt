package com.hypertrack.android.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatus

class VisitDetailsViewModel(
    private val visitsRepository: VisitsRepository,
    private val id: String
) : ViewModel() {

    val visit: LiveData<Visit> = visitsRepository.visitForId(id)
    private val _takePictureButton =  MediatorLiveData<Boolean>()
    private val _pickUpButton =  MediatorLiveData<Boolean>()
    private val _checkInButton =  MediatorLiveData<Boolean>()
    private val _checkOutButton =  MediatorLiveData<Boolean>()
    private val _cancelButton =  MediatorLiveData<Boolean>()
    private var _visitNote = MediatorLiveData<Pair<String, Boolean>>() //
    private var _showToast = MutableLiveData(false)
    private var updatedNote: String = visit.value?.visitNote?:""
    private var visitPhoto: String? = null

    init {
        _visitNote.addSource(visitsRepository.visitForId(id)) {
            _visitNote.postValue(it.visitNote to visitsRepository.canEdit(id))
        }

        for ((model, targetState) in listOf(
            _takePictureButton to VisitStatus.COMPLETED,
            _pickUpButton to VisitStatus.PICKED_UP,
            _checkInButton to VisitStatus.VISITED,
            _checkOutButton to VisitStatus.COMPLETED,
            _cancelButton to VisitStatus.CANCELLED
        )) {
            model.addSource(visit) {
                model.postValue(visitsRepository.transitionAllowed(targetState, id))
            }
            model.addSource(visitsRepository.isTracking) {
                model.postValue(visitsRepository.transitionAllowed(targetState, id))
            }
        }

    }

    val visitNote: LiveData<Pair<String, Boolean>>
        get() = _visitNote
    val showToast: LiveData<Boolean>
        get() = _showToast
    val takePictureButton: LiveData<Boolean>
        get() = _takePictureButton
    val pickUpButton: LiveData<Boolean>
        get() = _pickUpButton
    val checkInButton: LiveData<Boolean>
        get() =  _checkInButton
    val checkOutButton: LiveData<Boolean>
        get() = _checkOutButton
    val cancelButton: LiveData<Boolean>
        get() = _cancelButton

    fun onVisitNoteChanged(newNote : String) {
        Log.d(TAG, "onVisitNoteChanged $newNote")
        updatedNote = newNote
    }

    fun onPreviwIconAdded(encodedImage: Bitmap) {
        Log.v(TAG, "onPictureAdded handler $encodedImage")
        visitsRepository.addPreviewIcon(id, encodedImage)
        updateVisit()
    }

    fun onPickUpClicked() {
        Log.v(TAG, "PickUp click handler")
        visitsRepository.setPickedUp(id)
        updateVisit()
    }

    fun onCheckInClicked() {
        Log.v(TAG, "Lower button click handler")
        visitsRepository.setVisited(id)
        updateVisit()
    }

    fun onCheckOutClicked() {
        Log.v(TAG, "Check Out click handler")
        visitsRepository.setCompleted(id)
        updateVisit()
    }

    fun onCancelClicked() {
        Log.v(TAG, "Cancel click handler")
        visitsRepository.setCancelled(id)
        updateVisit()
    }

    fun getLatLng(): LatLng?  {
        visit.value?.latitude?.let { lat -> visit.value?.longitude?.let { lng -> return LatLng(lat, lng) } }
        return null
    }

    fun getLabel() : String = "Parcel ${visit.value?._id?:"unknown"}"

    fun onBackPressed() = updateVisit()

    private fun updateVisit() {
        val isNoteChanged = visitsRepository.updateVisitNote(id, updatedNote)
        if (isNoteChanged) _showToast.postValue(true)
    }

    companion object {const val TAG = "VisitDetailsVM"}
}
