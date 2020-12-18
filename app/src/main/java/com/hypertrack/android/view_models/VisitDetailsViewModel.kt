package com.hypertrack.android.view_models

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatus
import kotlin.math.max
import kotlin.math.min

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

    fun onPictureResult(path: String, imageView: ImageView) {
        Log.d(TAG, "onPicResult $path")
        // Get the dimensions of the View
        val targetW: Int = (210 * Resources.getSystem().displayMetrics.density).toInt()
        val targetH: Int = (160 * Resources.getSystem().displayMetrics.density).toInt()

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(path, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = max(1, min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(path, bmOptions)?.also { onPreviwIconAdded(it) }

    }

    companion object {const val TAG = "VisitDetailsVM"}
}
