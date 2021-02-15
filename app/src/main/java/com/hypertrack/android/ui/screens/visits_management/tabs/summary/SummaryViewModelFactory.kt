package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//todo global vm factory?
class SummaryViewModelFactory(
//    private val historyRepository: HistoryRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when(modelClass) {
            SummaryViewModel::class.java -> SummaryViewModel(/*historyRepository*/) as T
            else -> throw IllegalStateException()
        }
    }
}