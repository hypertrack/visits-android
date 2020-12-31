package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.utils.DeeplinkResultListener
import com.hypertrack.android.utils.Destination
import kotlinx.coroutines.launch

class SplashScreenViewModel(
    private val driverRepository: DriverRepo,
    private val accountRepository: AccountRepository
) : ViewModel(), DeeplinkResultListener  {

    private val _showSpinner = MutableLiveData(true)
    private val _noAccountFragment = MutableLiveData(false)
    private val _destination = MutableLiveData(Destination.SPLASH_SCREEN)

    /** Show a loading spinner if true */
    val spinner: LiveData<Boolean>
        get() = _showSpinner

    /** Show no-account error fragment if true */
    val noAccountFragment: LiveData<Boolean>
        get() = _noAccountFragment

    val destination: LiveData<Destination>
        get() = _destination

    fun login() {
        when {
            driverRepository.hasDriverId -> {
                // already logged in
                _showSpinner.postValue(false)
                _destination.postValue(Destination.PERMISSION_REQUEST)
            }
            accountRepository.isVerifiedAccount -> {
                // publishable key already verified
                _showSpinner.postValue(false)
                _destination.postValue(Destination.DRIVER_ID_INPUT)
            }
            else -> {
                Log.d(TAG, "No publishable key found")
                noPkHanlder()
            }
        }
    }

    override fun onDeeplinkResult(parameters: Map<String, Any>) {
        Log.d(TAG, "Got deeplink result $parameters")

        // Here we can inject obligatory input (publishable key and driver id)
        // as well as configuration parameters:
        //                  show_manual_visits (default false)
        //                  auto_check_in (default true)
        val key = parameters["publishable_key"] as String?
        val email = parameters["email"] as String?
        val driverId = parameters["driver_id"] as String?
        val showCheckIn = parameters["show_manual_visits"] as String? ?:""
        val autoCheckIn = parameters["auto_check_in"] as String? ?: ""
        val pickUpAllowed = parameters["pick_up_allowed"] as String? ?: ""
        Log.v(TAG, "Got email $email, pk $key, driverId, $driverId, showCheckIn $showCheckIn, " +
                "auto checking $autoCheckIn pickUp allowed $pickUpAllowed")
        if (key != null) {
            Log.d(TAG, "Got key $key")
            try {
                viewModelScope.launch {
                    val correctKey = accountRepository.onKeyReceived(key, showCheckIn, autoCheckIn, pickUpAllowed)
                    Log.d(TAG, "onKeyReceived finished")
                    if (correctKey) {
                        Log.d(TAG, "Key validated successfully")
                        _showSpinner.postValue(false)
                        driverId?.let { driverRepository.driverId = it}
                        email?.let { driverRepository.driverId = it }
                        _destination.postValue(Destination.DRIVER_ID_INPUT)
                    } else {
                        login()
                    }
                }
                Log.d(TAG, "coroutine finished")
                return
            } catch (e : Throwable) {
                Log.w(TAG, "Cannot validate the key", e)
                login()
            }
        } else {
            parameters["error"]?.let {  Log.e(TAG, "Deeplink processing failed. $it") }
            login()

        }
    }

    private fun noPkHanlder() {
        Log.e(TAG, "No publishable key")
        _showSpinner.postValue(false)
        _destination.postValue(Destination.LOGIN)

    }

    companion object {
        const val TAG = "SplashScreenVM"
    }

}
