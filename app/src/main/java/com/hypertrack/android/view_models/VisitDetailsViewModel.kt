package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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
    private var _visitNote = MediatorLiveData<Pair<String?, Boolean>>() //
    private var _upperButton = MediatorLiveData<Pair<ButtonLabel, Boolean>>() // (upperButtonModel(visit.value, visitsRepository.canEdit(id)))
    private var _lowerButton = MediatorLiveData<Pair<ButtonLabel, Boolean>>() // (lowerButtonModel(visit.value, visitsRepository.canEdit(id)))

    init {
        _visitNote.addSource(visitsRepository.visitForId(id)) {
            _visitNote.postValue(it.visitNote to visitsRepository.canEdit(id))
        }
        _visitNote.addSource(visitsRepository.isTracking) { _visitNote.postValue(null to it) }

        _upperButton.addSource(visitsRepository.visitForId(id)) {
            _upperButton.postValue(upperButtonModel(it, visitsRepository.canEdit(id)))
        }
        _upperButton.addSource(visitsRepository.isTracking) {
            _upperButton.postValue(upperButtonModel(visit.value, it))
        }

        _lowerButton.addSource(visitsRepository.visitForId(id)) {
            _lowerButton.postValue(lowerButtonModel(it, visitsRepository.canEdit(id)))
        }
        _lowerButton.addSource(visitsRepository.isTracking) {
            _lowerButton.postValue(lowerButtonModel(visit.value, it))
        }
    }

    val visitNote: LiveData<Pair<String?, Boolean>>
        get() = _visitNote
    val upperButton: LiveData<Pair<ButtonLabel, Boolean>>
        get() = _upperButton
    val lowerButton: LiveData<Pair<ButtonLabel, Boolean>>
        get() = _lowerButton

    fun onVisitNoteChanged(newNote : String) {
        Log.d(TAG, "onVisitNoteChanged $newNote")
        visitsRepository.updateVisitNote(id, newNote)
    }

    fun onUpperButtonClicked() {
        Log.v(TAG, "Upper button click handler")
        // depending on visit state that could means to pick up or complete (check out)

        visit.value?.let {
            when (it.state) {
                VisitStatus.PENDING -> visitsRepository.setPickedUp(id)
                VisitStatus.VISITED, VisitStatus.PICKED_UP -> visitsRepository.setCompleted(id, true)
                else -> Log.w(TAG, "Unexpected upper button click for state ${it.state} for visit id $id")
            }
        }

    }

    fun onLowerButtonClicked() {
        Log.v(TAG, "Lower button click handler")

        // depending on visit state that could means to check in or cancel

        visit.value?.let {
            when (it.state) {
                VisitStatus.PENDING, VisitStatus.PICKED_UP -> visitsRepository.setCheckedIn(id)
                VisitStatus.VISITED -> visitsRepository.setCancelled(id)
                else -> Log.w(TAG, "Unexpected upper button click for state ${it.state} for visit id $id")
            }
        }
    }

    fun getLatLng(): LatLng?  {
        visit.value?.latitude?.let { lat -> visit.value?.longitude?.let { lng -> return LatLng(lat, lng) } }
        return null
    }

    fun getLabel() : String = "Parcel ${visit.value?._id?:"unknown"}"

    fun onBackPressed() {
        visitsRepository.updateVisitNote(id, visit.value?.visitNote?:"")
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