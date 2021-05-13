package com.hypertrack.android.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.EMPTY_HISTORY
import com.hypertrack.android.models.History
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.models.HistoryResult
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.MockData
import com.hypertrack.android.utils.OsUtilsProvider

class HistoryRepository(
        private val apiClient: ApiClient,
        private val crashReportsProvider: CrashReportsProvider,
        private val osUtilsProvider: OsUtilsProvider
) {

    private val _history = MutableLiveData<History>()

    val history: LiveData<History>
        get() = _history

    suspend fun getHistory(): HistoryResult {
        val result = apiClient.getHistory(
                osUtilsProvider.getLocalDate(),
                osUtilsProvider.getTimeZoneId()
        )
//        val result = MockData.MOCK_HISTORY
        return when (result) {
            is History -> {
                _history.postValue(result)
                result
            }
            is HistoryError -> {
                result.error?.let { crashReportsProvider.logException(it) }
                _history.postValue(_history.value?: EMPTY_HISTORY)
                result
            }
        }
    }
}