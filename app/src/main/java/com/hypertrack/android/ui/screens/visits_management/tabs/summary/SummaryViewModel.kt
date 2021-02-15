package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.Summary
import com.hypertrack.android.ui.base.BaseStateViewModel
import kotlinx.coroutines.launch

class SummaryViewModel(
//    private val historyRepository: HistoryRepository
): BaseStateViewModel() {

    val summary = MutableLiveData<Summary>(Summary(
            1345,
            1235,
            1450000000,
            123450000,
            12345,
            12345,
            123,
    ))

    val loadingState = MutableLiveData<Boolean>(false)

    fun init() {
        viewModelScope.launch {
//            historyRepository.refreshHistory()
        }
    }

}