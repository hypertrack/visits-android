package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.History
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.models.HistoryTile
import com.hypertrack.android.models.asTiles
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.utils.TimeDistanceFormatter
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val timeDistanceFormatter: TimeDistanceFormatter
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

    val error = MutableLiveData<String?>(null)

    fun getHistory() {
        viewModelScope.launch {
            when (val res = historyRepository.getHistory()) {
                is HistoryError -> {
                    error.postValue(res.error?.message)
                }
                is History -> {
                    if (res.locationTimePoints.isEmpty()) {
                        error.postValue("No history is available.")
                    }

                }
            }
        }
    }

    companion object { const val TAG = "HistoryViewModel" }
}