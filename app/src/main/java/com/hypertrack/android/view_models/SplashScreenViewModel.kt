package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.ui.base.BaseStateViewModel
import com.hypertrack.android.ui.base.JustLoading
import com.hypertrack.android.ui.base.State
import com.hypertrack.android.utils.CrashReportsProvider
import kotlinx.coroutines.launch

class SplashScreenViewModel(
    private val driverRepository: DriverRepo,
    private val accountRepository: AccountRepository,
    val crashReportsProvider: CrashReportsProvider
) : BaseStateViewModel()  {

//    private val _destination = MutableLiveData(Destination.SPLASH_SCREEN)

    private fun login() = when {
        driverRepository.hasDriverId -> {
            // already logged in
            crashReportsProvider.setUserIdentifier(driverRepository.driverId)
            state.postValue(LoggedIn)
        }
        accountRepository.isVerifiedAccount -> {
            // publishable key already verified
            state.postValue(AccountVerified)
        }
        else -> {
            Log.e(TAG, "No publishable key")
            // Log.d(TAG, "No publishable key found")
            state.postValue(NoPublishableKey)
        }
    }

    fun handleDeeplink(parameters: Map<String, Any>) {
        // Log.d(TAG, "Got deeplink result $parameters")

        // Here we can inject obligatory input (publishable key and driver id)
        // as well as configuration parameters:
        //                  show_manual_visits (default false)
        //                  auto_check_in (default true)
        val key = parameters["publishable_key"] as String?
        val email = parameters["email"] as String?
        val driverId = parameters["driver_id"] as String?
        val showCheckIn = parameters["show_manual_visits"] as String? ?:""
        //todo useless? (always true)
        val autoCheckIn = parameters["auto_check_in"] as String? ?: ""
        val pickUpAllowed = parameters["pick_up_allowed"] as String? ?: ""
        // Log.v(TAG, "Got email $email, pk $key, driverId, $driverId, showCheckIn $showCheckIn, auto checking $autoCheckIn pickUp allowed $pickUpAllowed")
        if (key != null) {
            // Log.d(TAG, "Got key $key")
            try {
                state.value = JustLoading
                viewModelScope.launch {
                    val correctKey = accountRepository.onKeyReceived(key, showCheckIn, autoCheckIn, pickUpAllowed)
                    // Log.d(TAG, "onKeyReceived finished")
                    if (correctKey) {
                        // Log.d(TAG, "Key validated successfully")
                        driverId?.let { driverRepository.driverId = it}
                        email?.let { driverRepository.driverId = it }
                        state.value = KeyIsCorrect
                    } else {
                        login()
                    }
                }
                // Log.d(TAG, "coroutine finished")
            } catch (e : Throwable) {
                Log.w(TAG, "Cannot validate the key", e)
                login()
            }
        } else {
            parameters["error"]?.let {  Log.e(TAG, "Deeplink processing failed. $it") }
            login()
        }
    }

    companion object {
        const val TAG = "SplashScreenVM"
    }

    object KeyIsCorrect: State()
    object LoggedIn: State()
    object AccountVerified: State()
    object NoPublishableKey: State()

}
