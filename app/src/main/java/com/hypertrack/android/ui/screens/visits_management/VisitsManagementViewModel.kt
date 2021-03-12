package com.hypertrack.android.ui.screens.visits_management

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.hypertrack.android.models.HistoryError
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitListItem
import com.hypertrack.android.models.VisitStatusGroup
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.TrackingStateValue
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SuppressLint("NullSafeMutableLiveData")
class VisitsManagementViewModel(
    private val visitsRepository: VisitsRepository,
    private val historyRepository: HistoryRepository,
    private val accountRepository: AccountRepository,
    private val crashReportsProvider: CrashReportsProvider,
    accessTokenRepository: AccessTokenRepository
) : BaseViewModel() {

    val isTracking = visitsRepository.isTracking

    private val _clockInButtonText = MediatorLiveData<CharSequence>()

    init {
        _clockInButtonText.addSource(visitsRepository.isTracking) { tracking ->
            _clockInButtonText.postValue(if (tracking) "Clock Out" else "Clock In")
        }
        if (accountRepository.shouldStartTracking) {
            visitsRepository.startTracking()
            accountRepository.shouldStartTracking = false
        }
    }

    val clockInButtonText: LiveData<CharSequence>
        get() = _clockInButtonText

    private val _checkInButtonText = MediatorLiveData<LocalVisitCtaLabel>()

    init {
        if (accountRepository.isManualCheckInAllowed) {
            _checkInButtonText.addSource(visitsRepository.hasOngoingLocalVisit) { hasVisit ->
                _checkInButtonText.postValue(if (hasVisit) LocalVisitCtaLabel.CHECK_OUT else LocalVisitCtaLabel.CHECK_IN)
            }
        }
    }

    val deviceHistoryWebUrl = accessTokenRepository.deviceHistoryWebViewUrl

    val checkInButtonText: LiveData<LocalVisitCtaLabel>
        get() = _checkInButtonText

    private val _showSpinner = MutableLiveData(false)
    val showSpinner: LiveData<Boolean>
        get() = _showSpinner

    private val _showSync = MutableLiveData(false)
    val showSync: LiveData<Boolean>
        get() = _showSync

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String>
        get() = _showToast

    private val _enableCheckIn = MediatorLiveData<Boolean>()

    init {
        if (accountRepository.isManualCheckInAllowed) {
            _enableCheckIn.addSource(visitsRepository.isTracking) { _enableCheckIn.postValue(it) }
        } else {
            _enableCheckIn.postValue(false)
        }
    }

    val enableCheckIn: LiveData<Boolean>
        get() = _enableCheckIn

    val visits = visitsRepository.visitListItems

    private val _statusBarColor = MediatorLiveData<Int?>()

    init {
        _statusBarColor.addSource(visitsRepository.trackingState) {
            when (it) {
                TrackingStateValue.TRACKING -> _statusBarColor.postValue(R.color.colorTrackingActive)
                TrackingStateValue.STOP -> _statusBarColor.postValue(R.color.colorTrackingStopped)
                TrackingStateValue.DEVICE_DELETED, TrackingStateValue.ERROR -> _statusBarColor.postValue(
                    R.color.colorTrackingError
                )
                else -> _statusBarColor.postValue(null)
            }
        }
    }

    val statusBarColor: LiveData<Int?>
        get() = _statusBarColor

    private val _statusBarMessage = MediatorLiveData<StatusMessage>()

    init {
        _statusBarMessage.addSource(visitsRepository.trackingState) {
            _statusBarMessage.postValue(
                when (it) {
                    TrackingStateValue.DEVICE_DELETED -> StatusString(R.string.device_deleted)
                    TrackingStateValue.ERROR -> StatusString(R.string.generic_tracking_error)
                    else -> visitsRepository.visitListItems.value.asStats()
                }
            )
        }
        _statusBarMessage.addSource(visitsRepository.visitListItems) { visits ->
            when (_statusBarMessage.value) {
                is StatusString -> {
                }
                else -> _statusBarMessage.postValue(visits.asStats())
            }

        }
    }

    val statusBarMessage: LiveData<StatusMessage>
        get() = _statusBarMessage

    val showCheckIn: Boolean = accountRepository.isManualCheckInAllowed

    val error = MutableLiveData<String>()

    fun refreshVisits(block: () -> Unit) {

        if (_showSync.value == true) return block()
        _showSync.postValue(true)

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            crashReportsProvider.logException(throwable)
        }
        MainScope().launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                visitsRepository.refreshVisits()
            } catch (e: Throwable) {
                Log.e(TAG, "Got error $e refreshing visits")
                when (e) {
                    is java.net.UnknownHostException, is java.net.ConnectException, is java.net.SocketTimeoutException -> Log.i(
                        TAG,
                        "Failed to refresh visits",
                        e
                    )
                    else -> crashReportsProvider.logException(e)
                }
                _showToast.postValue("Can't refresh visits")
            } finally {
                _showSync.postValue(false)
                block()
            }
        }
    }

    fun refreshHistory() {
        MainScope().launch {
            historyRepository.getHistory().also {
                if (it is HistoryError) {
                    //todo
                    error.postValue(/*it.error?.message*/ MyApplication.context.getString(R.string.history_error))
                }
            }
        }
    }

    fun switchTracking() {
        _showSpinner.postValue(true)
        viewModelScope.launch {
            visitsRepository.switchTracking()
            _showSpinner.postValue(false)

        }
    }

    fun checkIn() = visitsRepository.processLocalVisit()

    fun possibleLocalVisitCompletion() = visitsRepository.checkLocalVisitCompleted()

    companion object {
        const val TAG = "VisitsManagementVM"
    }

}

fun List<VisitListItem>?.asStats(): VisitsStats = this?.let {
    VisitsStats(filterIsInstance<Visit>()
        .groupBy { it.state.group }
        .mapValues { (_, items) -> items.size })
} ?: VisitsStats(emptyMap())

sealed class StatusMessage
class StatusString(val stringId: Int) : StatusMessage()
class VisitsStats(val stats: Map<VisitStatusGroup, Int>) : StatusMessage()

enum class LocalVisitCtaLabel {
    CHECK_IN, CHECK_OUT
}