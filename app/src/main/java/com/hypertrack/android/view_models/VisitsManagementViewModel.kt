package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.*
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitListItem
import com.hypertrack.android.models.VisitStatusGroup
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.TrackingStateValue
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.hypertrack.logistics.android.github.R

class VisitsManagementViewModel(
    private val visitsRepository: VisitsRepository,
    accountRepository: AccountRepository,
    accessTokenRepository: AccessTokenRepository,
    private val crashReportsProvider: CrashReportsProvider
) : ViewModel() {

    private val _clockInButtonText = MediatorLiveData<CharSequence>()
    init {
        _clockInButtonText.addSource(visitsRepository.isTracking) { tracking ->
            _clockInButtonText.postValue(if (tracking) "Clock Out" else "Clock In")
        }
    }
    val clockInButtonText: LiveData<CharSequence>
        get() = _clockInButtonText

    private val _checkInButtonText = MediatorLiveData<CharSequence>()
    init {
        if (accountRepository.isManualCheckInAllowed) {
            _checkInButtonText.addSource(visitsRepository.hasOngoingLocalVisit) { hasVisit ->
                _checkInButtonText.postValue(if (hasVisit) "CheckOut" else "CheckIn")
            }
        }
    }
    val checkInButtonText: LiveData<CharSequence>
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
            when(it) {
                TrackingStateValue.TRACKING -> _statusBarColor.postValue(R.color.colorTrackingActive)
                TrackingStateValue.STOP -> _statusBarColor.postValue(R.color.colorTrackingStopped)
                TrackingStateValue.DEVICE_DELETED, TrackingStateValue.ERROR -> _statusBarColor.postValue(R.color.colorTrackingError)
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
                    else -> visitsRepository.visitListItems.value.asStats()                }
            )
        }
        _statusBarMessage.addSource(visitsRepository.visitListItems) { visits ->
            when (_statusBarMessage.value) {
                is StatusString -> Log.v(TAG, "Not updating message as it shows tracking info")
                else -> _statusBarMessage.postValue(visits.asStats())
            }

        }
    }
    val statusBarMessage: LiveData<StatusMessage>
        get() = _statusBarMessage

    val showCheckIn: Boolean = accountRepository.isManualCheckInAllowed

    val deviceHistoryWebViewUrl = accessTokenRepository.deviceHistoryWebViewUrl

    fun refreshVisits(block: () -> Unit) {
        Log.v(TAG, "Refresh visits")

        if (_showSync.value == true) return
        _showSync.postValue(true)

         val coroutineExceptionHandler = CoroutineExceptionHandler{_ , throwable ->
            Log.e(TAG, "Got error $throwable in coroutine")
        }
        MainScope().launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                visitsRepository.refreshVisits()
            } catch (e: Throwable) {
                Log.e(TAG, "Got error $e refreshing visits")
                crashReportsProvider.logException(e)
                _showToast.postValue("Can't refresh visits")
            } finally {
                _showSync.postValue(false)
                block()
            }
        }
    }

    fun switchTracking() {
        Log.v(TAG, "switchTracking")
        _showSpinner.postValue(true)
        viewModelScope.launch {
            visitsRepository.switchTracking()
            _showSpinner.postValue(false)

        }
    }

    fun checkIn() = visitsRepository.processLocalVisit()

    fun possibleLocalVisitCompletion() = visitsRepository.checkLocalVisitCompleted()

    companion object { const val TAG = "VisitsManagementVM" }

}

fun List<VisitListItem>?.asStats(): VisitsStats = this?.let {
        VisitsStats(filterIsInstance<Visit>()
            .groupBy { it.state.group }
            .mapValues { (_, items) -> items.size })
    }?: VisitsStats(emptyMap())

sealed class StatusMessage
class StatusString(val stringId: Int) : StatusMessage()
class VisitsStats(val stats: Map<VisitStatusGroup, Int>) : StatusMessage()