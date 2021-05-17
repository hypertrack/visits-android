package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import androidx.lifecycle.*
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.common.DateTimeUtils
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TimeDistanceFormatter
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch

class SummaryViewModel(
    private val historyRepository: HistoryRepository,
    private val osUtilsProvider: OsUtilsProvider,
    private val timeDistanceFormatter: TimeDistanceFormatter
) : BaseViewModel() {

    val summary: LiveData<List<SummaryItem>> = Transformations.map(historyRepository.history) {
        it.summary.let { summary ->
            listOf(
                SummaryItem(
                    R.drawable.ic_ht_eta,
                    osUtilsProvider.stringFromResource(R.string.summary_total_tracking_time),
                    DateTimeUtils.secondsToLocalizedString(summary.totalDuration)
                ),
                SummaryItem(
                    R.drawable.ic_ht_drive,
                    osUtilsProvider.stringFromResource(R.string.summary_drive),
                    DateTimeUtils.secondsToLocalizedString(summary.totalDriveDuration),
                    timeDistanceFormatter.formatDistance(summary.totalDriveDistance)
                ),
                SummaryItem(
                    R.drawable.ic_ht_walk,
                    osUtilsProvider.stringFromResource(R.string.summary_walk),
                    DateTimeUtils.secondsToLocalizedString(summary.totalWalkDuration),
                    osUtilsProvider.stringFromResource(R.string.steps, summary.stepsCount)
                ),
                SummaryItem(
                    R.drawable.ic_ht_stop,
                    osUtilsProvider.stringFromResource(R.string.summary_stop),
                    DateTimeUtils.secondsToLocalizedString(summary.totalStopDuration)
                ),
            )
        }
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
