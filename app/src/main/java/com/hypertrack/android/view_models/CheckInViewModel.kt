package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.HyperTrackService
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.utils.Destination
import kotlinx.coroutines.launch

class CheckInViewModel(
    private val driverRepo: DriverRepo,
    private val hyperTrackService: HyperTrackService,
    private val deliveriesApiClient: ApiClient
) : ViewModel() {

    private val _checkInButtonEnabled = MutableLiveData<Boolean>(false)

    private val _destination = MutableLiveData<Destination>(Destination.LOGIN)

    val enableCheckIn: LiveData<Boolean>
        get() = _checkInButtonEnabled

    val destination : LiveData<Destination>
        get() = _destination


    fun onTextChanged(input: CharSequence) {
        when {
            input.isNotEmpty() -> _checkInButtonEnabled.postValue(true)
            else -> _checkInButtonEnabled.postValue(false)
        }
    }

    fun onLoginClick(inputText: CharSequence?) {
        inputText?.let {
            _checkInButtonEnabled.postValue(false)
            val driverId = it.toString()
            Log.d(TAG, "Proceeding with Driver Id $driverId")
            hyperTrackService.driverId = driverId
            driverRepo.driverId = driverId
            viewModelScope.launch {
                deliveriesApiClient.checkinCall()
                _destination.postValue(Destination.LIST_VIEW)
            }
            return
        }

    }
    companion object {
        const val TAG = "CheckInVM"
    }
}