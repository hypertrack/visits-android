package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.repository.HistoryRepository
import kotlinx.coroutines.launch

class SummaryViewModel(
        private val historyRepository: HistoryRepository
) : ViewModel() {

    val summary = Transformations.map(historyRepository.history) {
        it.summary
    }

    val error = MutableLiveData<HistoryError>()

    fun refreshSummary() {
        viewModelScope.launch {
            val res = historyRepository.getHistory()
            if (res is HistoryError) {
                error.postValue(res)
            }
        }
    }
}
