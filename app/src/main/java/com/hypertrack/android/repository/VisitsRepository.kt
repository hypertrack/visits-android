package com.hypertrack.android.repository

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.*
import com.hypertrack.android.retryWithBackoff
import com.hypertrack.android.utils.*
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class VisitsRepository(
    private val osUtilsProvider: OsUtilsProvider,
    private val apiClient: ApiClient,
    private val visitsStorage: VisitsStorage,
    private val hyperTrackService: HyperTrackService,
    private val accountPreferences: AccountPreferencesProvider,
    private val imageDecoder: ImageDecoder,
    private val crashReportsProvider: CrashReportsProvider
) {

    private val _visitsMap: MutableMap<String, Visit>
            = visitsStorage.restoreVisits().associateBy { it._id  }.toMutableMap()

    private val _visitListItems: MutableLiveData<List<VisitListItem>>
            = MutableLiveData(_visitsMap.values.sortedWithHeaders())

    private val _visitItemsById: MutableMap<String, MutableLiveData<Visit>>
            = _visitsMap.mapValues { MutableLiveData(it.value) }.toMutableMap()

    val visitListItems: LiveData<List<VisitListItem>>
        get() = _visitListItems

    private val _hasOngoingLocalVisit = MutableLiveData(_visitsMap.getLocalVisit() != null)
    val hasOngoingLocalVisit: LiveData<Boolean>
        get() = _hasOngoingLocalVisit

    val trackingState: LiveData<TrackingStateValue>
        get() = hyperTrackService.state

    private val _isTracking = MediatorLiveData<Boolean>()
    init {
        _isTracking.addSource(hyperTrackService.state) {
            _isTracking.postValue(it == TrackingStateValue.TRACKING)
        }
    }

    val isTracking: LiveData<Boolean>
        get() = _isTracking


    suspend fun refreshVisits() {
        Log.v(TAG, "Refreshing visits")
        val geofences = apiClient.getGeofences()
        Log.v(TAG, "Got geofences $geofences")
        val trips = apiClient.getTrips()
        Log.v(TAG, "Got trips $trips")
        val prototypes : Set<VisitDataSource> = trips.union(geofences)
        Log.d(TAG, "Total prototypes $prototypes")
        prototypes.forEach { prototype ->
            Log.v(TAG, "Processing prototype $prototype")
            val currentValue = _visitsMap[prototype._id]
            if (currentValue == null) {
                val visit = Visit(
                    prototype,
                    osUtilsProvider,
                    accountPreferences
                )
                _visitsMap[visit._id] = visit
                _visitItemsById[visit._id] = MutableLiveData(visit)
            } else {
                val newValue = currentValue.update(prototype, accountPreferences.isAutoCheckInEnabled)
                _visitsMap[prototype._id] = newValue
                // getValue/postValue invocations below are called on different instances:
                // `getValue` is called on Map with default value
                // while `postValue` is for MutableLiveData
                _visitItemsById[prototype._id]?.postValue(newValue) // updates MutableLiveData
            }
        }
        val deletedEntries = _visitsMap.filter { it.value.isDeletable }.keys - prototypes.map { it._id }
        Log.v(TAG, "Entries missing in update and will be deleted $deletedEntries")
        _visitsMap -= deletedEntries
        _visitItemsById -= deletedEntries

        Log.d(TAG, "Updated _visitsMap $_visitsMap")
        Log.d(TAG, "Updated _visitItemsById $_visitItemsById")

        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        Log.d(TAG, "Updated _visitListItems ${_visitListItems.value}")
    }

    fun visitForId(id: String): LiveData<Visit> = _visitItemsById[id]?:throw IllegalArgumentException("No visit for id $id")

    fun updateVisitNote(id: String, newNote: String): Boolean {
        val target = _visitsMap[id] ?: return false
        Log.d(TAG, "Updating visit $target with note $newNote")
        // Brake infinite cycle
        if (target.visitNote == newNote) return false

        val updatedVisit = target.updateNote(newNote)
        updateItem(id, updatedVisit)
        Log.d(TAG, "Updated visit $updatedVisit")
        return true
    }

    private fun updateItem(id: String, updatedVisit: Visit) {
        _visitsMap[id] = updatedVisit
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id]?.postValue(updatedVisit)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
    }

    fun setPickedUp(id: String, newNote: String? = null) {
        Log.v(TAG, "Set picked UP $id")
        val target = _visitsMap[id] ?: return
        if (target.tripVisitPickedUp) return
        val updatedVisit = target.pickUp(newNote)
        Log.v(TAG, "Marked order $target as picked up")
        hyperTrackService.sendPickedUp(id, target.typeKey)
        updateItem(id, updatedVisit)
    }

    fun setVisited(id: String, newNote: String? = null) {
        Log.v(TAG, "Set checked in $id")
        val target = _visitsMap[id] ?: return
        val updatedVisit = target.markVisited(newNote)
        Log.v(TAG, "Marked order $target as checked in")
        hyperTrackService.createVisitStartEvent(id, target.typeKey)
        updateItem(id, updatedVisit)
    }

    fun setCompleted(id: String, newNote: String? = null) {
        Log.v(TAG, "setCompleted $id $newNote")
        val target = _visitsMap[id] ?: return
        if (target.isCompleted) return
        val completedVisit = target.complete(osUtilsProvider.getCurrentTimestamp(), newNote)
        Log.d(TAG, "Completed visit $completedVisit ")
        hyperTrackService.sendCompletionEvent(id, completedVisit.visitNote, completedVisit.typeKey, true, completedVisit.visitPicture)
        updateItem(id, completedVisit)
    }

    fun setCancelled(id: String, newNote: String? = null) {
        Log.v(TAG, "setCancelled $id $newNote")
        val target = _visitsMap[id] ?: return
        if (target.isCompleted) return
        val completedVisit = target.cancel(osUtilsProvider.getCurrentTimestamp(), newNote)
        Log.d(TAG, "Cancelled visit $completedVisit")
        hyperTrackService.sendCompletionEvent(id, completedVisit.visitNote, completedVisit.typeKey, false, completedVisit.visitPicture)
        updateItem(id, completedVisit)
    }

    fun switchTracking() {
        Log.d(TAG, "switch Tracking")
        if (_isTracking.value == true) {
            Log.v(TAG, "Stop tracking")
            hyperTrackService.clockOut()
        } else {
            Log.v(TAG, "Start tracking")
            hyperTrackService.clockIn()
        }
    }

    fun processLocalVisit() {
        Log.d(TAG, "processLocalVisit")
        val localVisit = _visitsMap.getLocalVisit()
        localVisit?.let { ongoingVisit ->
            setCompleted(ongoingVisit._id)
            _hasOngoingLocalVisit.postValue(false)
            return
        }

        val createdAt = osUtilsProvider.getCurrentTimestamp()
        val newLocalVisit = Visit(
            _id = UUID.randomUUID().toString(),
            visit_id = osUtilsProvider.getString(R.string.local_visit_on) + osUtilsProvider.getFineDateTimeString(),
            createdAt = createdAt,
            visitedAt = createdAt,
            visitType = VisitType.LOCAL,
            _state = VisitStatus.VISITED
        )
        val id = newLocalVisit._id
        _visitsMap[id] = newLocalVisit
        hyperTrackService.createVisitStartEvent(id, newLocalVisit.typeKey)
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id] = MutableLiveData(newLocalVisit)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        _hasOngoingLocalVisit.postValue(true)
    }

    fun checkLocalVisitCompleted() = _hasOngoingLocalVisit.postValue(
        _visitsMap.getLocalVisit()?.let { true } ?: false
    )

    fun canEdit(visitId: String) = visitForId(visitId).value?.isEditable?:false

    fun transitionAllowed(targetState: VisitStatus, visitId: String): Boolean {
        Log.v(TAG, "transitionAllowed $targetState, $visitId")
        if (targetState == VisitStatus.VISITED && accountPreferences.isAutoCheckInEnabled) return false
        val visit = visitForId(visitId).value!!
        if (visit.state == VisitStatus.COMPLETED) return false
        return visit.state.canTransitionTo(targetState) && isTracking.value == true
    }

    suspend fun setImage(id: String, imagePath: String) = coroutineScope {
        Log.d(TAG, "Update image for visit $id")

        val target = _visitsMap[id] ?: return@coroutineScope
        val previewMaxSideLength: Int = (200 * Resources.getSystem().displayMetrics.density).toInt()
        launch {
            target.icon = imageDecoder.readBitmap(imagePath, previewMaxSideLength)
            Log.v(TAG, "Updated icon in target $target")
            updateItem(id, target)
        }

        Log.d(TAG, "Launched preview update task")
        try {
            retryWithBackoff(
                times = 5, factor = 10.0,
                block = { uploadImage(imagePath, target, id) }
            )
        } catch (t: Throwable) {
            crashReportsProvider.logException(t)
        }
    }

    private suspend fun uploadImage(
        imagePath: String,
        target: Visit,
        id: String
    )  {
        val uploadedImage = imageDecoder.readBitmap(imagePath, MAX_IMAGE_SIDE_LENGTH_PX)
        val imageKey = apiClient.uploadImage(uploadedImage)
        target.visitPicture = imageKey
        updateItem(id, target)
        Log.v(TAG, "Updated visit pic in target $target")
        File(imagePath).apply { if (exists()) delete() }
    }

    companion object { const val TAG = "VisitsRepository"}

}

private fun Collection<Visit>.sortedWithHeaders(): List<VisitListItem> {
    val grouped = this.groupBy { it.state.group }
    val result = ArrayList<VisitListItem>(this.size + grouped.keys.size)
    grouped.keys.sortedBy { it.ordinal }.forEach { visitStatus ->
        result.add(HeaderVisitItem(visitStatus))
        result.addAll(
            grouped[visitStatus]
                ?.sortedWith(compareBy { it.createdAt })
                ?.reversed()
                ?: emptyList()
        )
    }
    return result
}

private fun Map<String, Visit>.getLocalVisit(): Visit? =
    values.firstOrNull { it.isLocal && !it.isCompleted }
