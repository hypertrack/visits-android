package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.*
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TimeDistanceFormatter
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val timeDistanceFormatter: TimeDistanceFormatter,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val history = historyRepository.history

    val tiles = MediatorLiveData<List<HistoryTile>>()

    init {
        tiles.addSource(historyRepository.history) {
            if (it.locationTimePoints.isNotEmpty()) {
                Log.d(TAG, "got new history $it")
                val asTiles = it.asTiles(timeDistanceFormatter)
                Log.d(TAG, "converted to tiles $asTiles")
                tiles.postValue(asTiles)
            } else {
                Log.d(TAG, "Empty history")
                tiles.postValue(emptyList())
            }
        }
    }

    val error = SingleLiveEvent<String?>()

    fun refreshHistory() {
        viewModelScope.launch {
            when (val res = historyRepository.getHistory()) {
                is HistoryError -> {
                    error.postValue(res.error?.message)
                }
                is History -> {
//                    if (res.locationTimePoints.isEmpty()) {
//                        error.postValue("No history is available.")
//                    }
                }
            }
        }
    }

    fun onCopyClick(historyTile: HistoryTile) {
        osUtilsProvider.copyToClipboard(historyTile.marker?.let { marker ->
            when (marker.type) {
                MarkerType.GEOTAG -> {
                    (marker as GeoTagMarker).metadata.let {
                        it["visit_id"]?.toString() ?: it.toString()
                    }
                }
                else -> null
            }
        } ?: historyTile.address?.toString() ?: historyTile.description.toString())
    }

    companion object {
        const val TAG = "HistoryViewModel"
    }
}