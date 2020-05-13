package com.hypertrack.android.view_models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.getServiceLocator
import kotlinx.coroutines.launch

class CheckInViewModel(application: Application) : AndroidViewModel(application) {

    private val driverRepo: DriverRepo = application.getServiceLocator().getDriverRepo()

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
            getApplication<Application>().getServiceLocator()
                .getHyperTrack().setDeviceName(driverId)
            driverRepo.driverId = driverId
            viewModelScope.launch {
                getApplication<Application>().getServiceLocator().getDeliveriesApiClient()
                    .checkinCall()
                _destination.postValue(Destination.LIST_VIEW)
            }
            return
        }

    }
    companion object {
        const val TAG = "CheckInVM"
    }
}