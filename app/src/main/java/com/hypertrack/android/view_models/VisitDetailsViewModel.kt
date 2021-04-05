package com.hypertrack.android.view_models

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.interactors.VisitsInteractor
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitPhotoState
import com.hypertrack.android.models.VisitStatus
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.screens.visit_details.VisitPhotoItem
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class VisitDetailsViewModel(
    private val visitsRepository: VisitsRepository,
    private val visitsInteractor: VisitsInteractor,
    private val id: String
) : BaseViewModel() {

    val visit: LiveData<Visit> = visitsRepository.visitForId(id)

    val visitPhotos = Transformations.map(visit) { visit ->
        visit.photos
    }

    val photoError = visitsInteractor.photoErrorFlow.asLiveData()

    private val _takePictureButton = MediatorLiveData<Boolean>()
    private val _pickUpButton = MediatorLiveData<Boolean>()
    private val _checkInButton = MediatorLiveData<Boolean>()
    private val _checkOutButton = MediatorLiveData<Boolean>()
    private val _cancelButton = MediatorLiveData<Boolean>()
    private var _visitNote = MediatorLiveData<Pair<String, Boolean>>() //
    private var updatedNote: String? = null

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

    val message = MutableLiveData<String>()

    val visitNote: LiveData<Pair<String, Boolean>>
        get() = _visitNote

    val takePictureButton: LiveData<Boolean>
        get() = _takePictureButton
    val pickUpButton: LiveData<Boolean>
        get() = _pickUpButton
    val checkInButton: LiveData<Boolean>
        get() = _checkInButton
    val checkOutButton: LiveData<Boolean>
        get() = _checkOutButton
    val cancelButton: LiveData<Boolean>
        get() = _cancelButton

    fun onVisitNoteChanged(newNote: String) {
        // Log.d(TAG, "onVisitNoteChanged $newNote")
        updatedNote = newNote
    }

    fun onPickUpClicked() = visitsRepository.setPickedUp(id, updatedNote)

    fun onCheckOutClicked() {
        visitsRepository.setCompleted(id, updatedNote)
    }

    fun onCancelClicked() {
        visitsRepository.setCancelled(id, updatedNote)
    }

    fun getLatLng(): LatLng? {
        visit.value?.latitude?.let { lat -> visit.value?.longitude?.let { lng -> return LatLng(lat, lng) } }
        return null
    }

    fun getLabel(): String = "Parcel ${visit.value?._id ?: "unknown"}"

    fun onBackPressed() = updateNote()

    private fun updateNote() {
        // Log.v(TAG, "updateNote")
        updatedNote?.let {
            if (visitsRepository.updateVisitNote(id, it))
                message.postValue(MyApplication.context.getString(R.string.vist_note_updated))
        }
    }

    fun onPictureResult(path: String) {
        // Log.d(TAG, "onPicResult $path")
        MainScope().launch {
            visitsInteractor.addPhotoToVisit(id, path)
        }
    }

    fun onPhotoClicked(visitPhotoItem: VisitPhotoItem) {
        if (visitPhotoItem.visitPhoto.state == VisitPhotoState.ERROR) {
            visitsInteractor.retryVisitPhotoUpload(id, visitPhotoItem.visitPhoto)
        }
    }

    fun requestVisitNoteUpdate() {
        updateNote()
    }

    companion object {
        const val TAG = "VisitDetailsVM"
    }
}
