package com.hypertrack.android.view_models

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.hypertrack.android.repository.CheckInRepo
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.utils.getServiceLocator

class CheckInViewModel(application: Application) : AndroidViewModel(application) {


    private val _checkInButtonEnabled = MutableLiveData<Boolean>(false)


    val driverRepo: DriverRepo = application.getServiceLocator().getDriverRepo()

    val enableCheckIn: LiveData<Boolean>
        get() = _checkInButtonEnabled


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
            return
        }
        // TODO Denys: show error?

    }
    companion object {
        const val TAG = "CheckInVM"
    }
}