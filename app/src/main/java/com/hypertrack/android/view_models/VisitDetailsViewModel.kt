package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.repository.Visit

class VisitDetailsViewModel(
    private val visitsRepository: VisitsRepository,
    private val id: String
) : ViewModel() {

    val visit: LiveData<Visit> = visitsRepository.visitForId(id)
    private var visitNote = visit.value?.visitNote?:""

    private val _showNoteUpdatedToast = MutableLiveData(false)

    val showNoteUpdatedToast: LiveData<Boolean>
        get() = _showNoteUpdatedToast


    fun onVisitNoteChanged(newNote : String) {
        Log.d(TAG, "onVisitNoteChanged $newNote")
        visitNote = newNote
    }

    fun onMarkedCompleted() {
        val noteChanged = visitsRepository.updateVisitNote(id, visitNote)
        _showNoteUpdatedToast.postValue(noteChanged)
        visitsRepository.markCompleted(id)
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