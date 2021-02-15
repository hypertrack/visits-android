package com.hypertrack.android.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.HistoryRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
): ViewModel() {

    val history = historyRepository.history

    fun getHistory() {

        viewModelScope.launch { historyRepository.getHistory() }
    }
}