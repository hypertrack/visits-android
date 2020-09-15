package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.*
import com.hypertrack.android.utils.VisitsStorage
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TrackingStateValue
import java.util.*
import kotlin.collections.ArrayList

const val COMPLETED = "Completed"
const val VISITED = "Visited"
const val PENDING = "Pending"

class VisitsRepository(
    private val osUtilsProvider: OsUtilsProvider,
    private val apiClient: ApiClient,
    private val visitsStorage: VisitsStorage,
    private val hyperTrackService: HyperTrackService
) {

    private val _visitsMap: MutableMap<String, Visit>
            = visitsStorage.restoreVisits().associateBy { it._id  }.toMutableMap()

    private val _visitListItems: MutableLiveData<List<VisitListItem>>
            = MutableLiveData(_visitsMap.values.sortedWithHeaders())

    private val _visitItemsById: MutableMap<String, MutableLiveData<Visit>>
            = _visitsMap.mapValues { MutableLiveData(it.value) }.toMutableMap()

    val visitListItems: LiveData<List<VisitListItem>>
        get() = _visitListItems

    private val _status = MediatorLiveData<Pair<TrackingStateValue, String>>()

    private val _hasOngoingLocalVisit = MutableLiveData<Boolean>(_visitsMap.getLocalVisit() != null)

    val hasOngoingLocalVisit: LiveData<Boolean>
        get() = _hasOngoingLocalVisit

    init{
        _status.addSource(hyperTrackService.state) { state ->
            val label = _status.value?.second?:""
            _status.postValue(state to label)
        }
        _status.addSource(visitListItems) { items ->
            val trackingState = _status.value?.first?:TrackingStateValue.UNKNOWN
            val label = items.toStatusLabel()
            val fineLabel = if (label.isNotEmpty()) label else "No planned visits"
            _status.postValue(trackingState to fineLabel)
        }
    }

    val statusLabel: LiveData<Pair<TrackingStateValue, String>>
        get() = _status

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
            val currentValue = _visitsMap[prototype.visitId]
            if (currentValue == null) {
                val visit = Visit(
                    prototype,
                    osUtilsProvider
                )
                _visitsMap[visit._id] = visit
                _visitItemsById[visit._id] = MutableLiveData(visit)
            } else {
                val newValue = currentValue.update(prototype)
                _visitsMap[prototype.visitId] = newValue
                // getValue/postValue invocations below are called on different instances:
                // `getValue` is called on Map with default value
                // while `postValue` is for MutableLiveData
                _visitItemsById[prototype.visitId]?.postValue(newValue) // updates MutableLiveData
            }
        }
        val deletedEntries = _visitsMap.filter { it.value.isNotLocal }.keys - prototypes.map { it.visitId }
        Log.v(TAG, "Entries missing in update and will be deleted $deletedEntries")
        _visitsMap -= deletedEntries
        _visitItemsById -= deletedEntries

        Log.d(TAG, "Updated _visitsMap $_visitsMap")
        Log.d(TAG, "Updated _visitItemsById $_visitItemsById")

        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        Log.d(TAG, "Updated _visitListItems $_visitListItems")
    }

    fun visitForId(id: String): LiveData<Visit> {
           return _visitItemsById[id]?:throw IllegalArgumentException("No visit for id $id")
        }

    fun updateVisitNote(id: String, newNote: String): Boolean {
        Log.d(TAG, "Updating visit $id with note $newNote")
        val target = _visitsMap[id] ?: return false
        // Brake infinite cycle
        if (target.visitNote == newNote) return false

        val updatedVisit = target.updateNote(newNote)
        _visitsMap[id] = updatedVisit
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id]?.postValue(updatedVisit)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        Log.d(TAG, "Updated visit $updatedVisit")
        return true
    }

    fun markCompleted(id: String) {
        val target = _visitsMap[id] ?: return
        if (target.isCompleted) return
        val completedVisit = target.complete(osUtilsProvider.getCurrentTimestamp())
        _visitsMap[id] = completedVisit
        Log.d(TAG, "Completed visit $completedVisit")
        hyperTrackService.sendCompletionEvent(id, completedVisit.visitNote, completedVisit.typeKey)
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id]?.postValue(completedVisit)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
    }

    suspend fun switchTracking() {
        Log.d(TAG, "switch Tracking")
        if (_isTracking.value == true) {
            Log.v(TAG, "Stop tracking")
            apiClient.clockOut()
        } else {
            Log.v(TAG, "Start tracking")
            apiClient.clockIn()
        }
    }

    fun processLocalVisit() {
        Log.d(TAG, "processLocalVisit")
        val localVisit = _visitsMap.getLocalVisit()
        localVisit?.let { ongoingVisit ->
            markCompleted(ongoingVisit._id)
            _hasOngoingLocalVisit.postValue(false)
            return
        }

        val createdAt = osUtilsProvider.getCurrentTimestamp()
        val newLocalVisit = Visit(
            _id = UUID.randomUUID().toString(),
            visit_id = "Visit on ${osUtilsProvider.getFineDateTimeString()}",
            createdAt = createdAt,
            enteredAt = createdAt,
            visitType = VisitType.LOCAL
        )
        val id = newLocalVisit._id
        _visitsMap[id] = newLocalVisit
        hyperTrackService.createVisitStartEvent(id, newLocalVisit.typeKey)
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id] = MutableLiveData(newLocalVisit)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        _hasOngoingLocalVisit.postValue(true)

    }

    fun checkLocalVisitCompleted() {
        val isNotCompleted = _visitsMap.getLocalVisit()?.let { true } ?: false
        _hasOngoingLocalVisit.postValue(isNotCompleted)
    }

    companion object { const val TAG = "VisitsRepository"}

}

private fun Collection<Visit>.sortedWithHeaders(): List<VisitListItem> {
    val grouped = this.groupBy { it.status }
    val result = ArrayList<VisitListItem>(this.size + grouped.keys.size)
    grouped.keys.forEach { visitStatus ->
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

private fun List<VisitListItem>.toStatusLabel(): String {
    return filterIsInstance<Visit>()
        .groupBy { it.status }
        .entries.
        fold("")
        {acc, entry -> acc + "${entry.value.size} ${entry.key} Item${if (entry.value.size == 1) " " else "s "}"}
}

private fun Map<String, Visit>.getLocalVisit(): Visit? {
    val ongoingLocal = values.filter { it.isLocal }.filter { !it.isCompleted }
    if (ongoingLocal.isEmpty()) return null
    return ongoingLocal.first()
}




