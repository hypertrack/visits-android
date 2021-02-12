package com.hypertrack.android.view_models

import androidx.lifecycle.ViewModel
import com.hypertrack.android.repository.HistoryRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HistoryViewModel(
    val historyRepository: HistoryRepository
): ViewModel() {

    val history = historyRepository.history

    fun getHistory() {

        MainScope().launch { historyRepository.getHistory() }
    }
}