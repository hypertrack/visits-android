package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.ui.base.BaseStateViewModel
import kotlinx.coroutines.launch

class SummaryViewModel(
        private val historyRepository: HistoryRepository
) : BaseStateViewModel() {

    val summary = Transformations.map(historyRepository.history) {
        it.summary
    }

    fun refreshSummary() {
        viewModelScope.launch {
            historyRepository.getHistory()
        }
    }
}
