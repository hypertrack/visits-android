package com.hypertrack.android.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.repository.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
): ViewModel() {

    val history = historyRepository.history

    val error = MutableLiveData<HistoryError>()

    fun getHistory() {
        viewModelScope.launch {
            val res = historyRepository.getHistory()
            if(res is HistoryError) {
                error.postValue(res)
            }
        }
    }
}