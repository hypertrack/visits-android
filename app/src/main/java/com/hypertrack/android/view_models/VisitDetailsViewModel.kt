package com.hypertrack.android.view_models

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
    private var _visitNote = MutableLiveData((visit.value?.visitNote ?:"") to visitsRepository.canEdit(id))
    private var _upperButton = MutableLiveData(upperButtonModel(visit.value, visitsRepository.canEdit(id)))
    private var _lowerButton = MutableLiveData(lowerButtonModel(visit.value, visitsRepository.canEdit(id)))

    val visitNote: LiveData<Pair<String, Boolean>>
        get() = _visitNote
    val upperButton: LiveData<Pair<ButtonLabel, Boolean>>
        get() = _upperButton
    val lowerButton: LiveData<Pair<ButtonLabel, Boolean>>
        get() = _lowerButton

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
    }

    fun onUpperButtonClicked() {
        // depending on visit state that could means to pick up or complete (check out)

//        _showNoteUpdatedToast.postValue(visitsRepository.updateVisitNote(id, visitNote))
//        visitsRepository.markCompleted(id, isCompleted)
    }

    fun onLowerButtonClicked() {
        val wasCancelled = visit.value?.tripVisitPickedUp ?: false
        if (wasCancelled) {
            onUpperButtonClicked()
        } else {
//            _showNoteUpdatedToast.postValue(visitsRepository.updateVisitNote(id, visitNote))
            visitsRepository.setPickedUp(id)
        }
    }

    fun getLatLng(): LatLng?  {
        visit.value?.latitude?.let { lat -> visit.value?.longitude?.let { lng -> return LatLng(lat, lng) } }
        return null
    }

    fun getLabel() : String = "Parcel ${visit.value?._id?:"unknown"}"

    fun onBackPressed() {
//        val noteChanged = visitsRepository.updateVisitNote(id, visitNote)
//        _showNoteUpdatedToast.postValue(noteChanged)

    }

    companion object {const val TAG = "VisitDetailsVM"}
}

private fun upperButtonModel(visit: Visit?, canEdit: Boolean): Pair<ButtonLabel, Boolean> = when (visit?.state) {
    VisitStatus.PENDING -> ButtonLabel.PICK_UP to canEdit
    VisitStatus.PICKED_UP -> ButtonLabel.PICK_UP to false
    VisitStatus.VISITED -> ButtonLabel.CHECK_OUT to canEdit
    else -> ButtonLabel.CHECK_OUT to false
}

private fun lowerButtonModel(visit: Visit?, canEdit: Boolean): Pair<ButtonLabel, Boolean> = when (visit?.state) {
    VisitStatus.PENDING, VisitStatus.PICKED_UP -> ButtonLabel.CHECK_IN to canEdit
    VisitStatus.VISITED -> ButtonLabel.CANCEL to canEdit
    else -> ButtonLabel.CANCEL to false
}

enum class ButtonLabel {PICK_UP, CHECK_IN, CHECK_OUT, CANCEL}