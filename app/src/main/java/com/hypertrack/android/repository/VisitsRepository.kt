package com.hypertrack.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
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

        val geofences = apiClient.getVisits()
        geofences.forEach { geofence ->
            Log.d(TAG, "Processing geofence $geofence")
            val currentValue = _visitsMap[geofence.geofence_id]
            if (currentValue == null) {
                val visit = Visit(geofence, osUtilsProvider)
                _visitsMap[visit._id] = visit
                _visitItemsById[visit._id] = MutableLiveData(visit)
            } else {
                val newValue = currentValue.update(geofence)
                _visitsMap[geofence.geofence_id] = newValue
                // getValue/postValue invocations below are called on different instances:
                // `getValue` is called on Map with default value
                // while `postValue` is for MutableLiveData
                _visitItemsById[geofence.geofence_id]?.postValue(newValue) // updates MutableLiveData
            }
        }
        val deletedEntries = _visitsMap.filter { it.value.isNotLocal }.keys - geofences.map { it.geofence_id }
        Log.d(TAG, "Entries missing in update and will be deleted $deletedEntries")
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

        val updatedNote = target.updateNote(newNote)
        _visitsMap[id] = updatedNote
        hyperTrackService.sendUpdatedNote(id, newNote)
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id]?.postValue(updatedNote)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        return true
    }

    fun markCompleted(id: String) {
        val target = _visitsMap[id] ?: return
        if (target.isCompleted) return
        val completedVisit = target.complete(osUtilsProvider.getCurrentTimestamp())
        _visitsMap[id] = completedVisit
        hyperTrackService.sendCompletionEvent(id)
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

        val newLocalVisit = Visit(
            _id=UUID.randomUUID().toString(),
            createdAt = osUtilsProvider.getCurrentTimestamp(),
            enteredAt = osUtilsProvider.getCurrentTimestamp()
        )
        val id = newLocalVisit._id
        _visitsMap[id] = newLocalVisit
        hyperTrackService.createVisitStartEvent(id)
        visitsStorage.saveVisits(_visitsMap.values.toList())
        _visitItemsById[id] = MutableLiveData(newLocalVisit)
        _visitListItems.postValue(_visitsMap.values.sortedWithHeaders())
        _hasOngoingLocalVisit.postValue(true)

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

sealed class VisitListItem
data class HeaderVisitItem(val text: String) : VisitListItem()
data class Visit(val _id: String,
                 val visit_id: String = "", val driver_id: String = "", val customerNote: String = "",
                 val createdAt: String = "", val address: Address = Address(
        "",
        "",
        "",
        ""
    ),
                 val visitNote: String = "", var visitPicture: String = "",
                 var enteredAt:String = "",
                 val completedAt: String = "", val exitedAt: String = "",
                 val latitude: Double? = null, val longitude: Double? = null): VisitListItem() {
    val isCompleted: Boolean
        get() = status == COMPLETED

    val status: String
        get() = when {
                completedAt.isNotEmpty() -> COMPLETED
                enteredAt.isNotEmpty() -> VISITED
                else -> PENDING
            }

    val isLocal = !isNotLocal

    val isNotLocal:Boolean
        get() = (latitude != null && longitude != null)

    fun hasPicture() = visitPicture.isNotEmpty()

    fun hasNotes() = visitNote.isNotEmpty()

    fun update(geofence: Geofence) : Visit {

        return if (toNote(geofence.metadata) == customerNote) this
            else Visit(
                _id, visit_id, driver_id, toNote(geofence.metadata),
                createdAt, address, visitNote, visitPicture, enteredAt,
                completedAt, exitedAt, latitude, longitude
            )
        // TODO Denys - update when API adds support to geofence events
//        when {
//            (geofence.entered_at != enteredAt) -> pass
//            (geofence.exited_at != exitedAt) -> pass
//            (geofence.metadata.toString() != customerNote) -> pass
//            else -> return this
//        }
//        return Visit(_id, visit_id, driver_id, geofence.metadata.toString(),
//        createdAt, updatedAt, address, visitNote, visitPicture, geofence.entered_at,
//            completedAt, geofence.exited_at, latitude, longitude)

    }

    fun updateNote(newNote: String): Visit {
        return Visit(_id, visit_id, driver_id, customerNote,
        createdAt, address, newNote, visitPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude)
    }

    fun complete(completedAt: String): Visit {
        return Visit(_id, visit_id, driver_id, customerNote,
            createdAt, address, visitNote, visitPicture, enteredAt,
            completedAt, exitedAt, latitude, longitude)
    }

    constructor(geofence: Geofence, osUtilsProvider: OsUtilsProvider) : this(
        _id = geofence.geofence_id,
        customerNote = toNote(geofence.metadata),
        address = osUtilsProvider.getAddressFromCoordinates(geofence.latitude, geofence.longitude),
        createdAt = geofence.created_at,
//        enteredAt = geofence.entered_at, completedAt = geofence.completed_at,
    latitude = geofence.latitude, longitude = geofence.longitude)

}

private fun toNote(metadata: Map<String, Any>?): String {
    if (metadata == null) return ""
    val result = StringBuilder()
    metadata.forEach { (key, value) -> result.append("$key: $value\n") }
    return result.toString().dropLast(1)
}


data class Address (val street : String, val postalCode : String, val city : String, val country : String)