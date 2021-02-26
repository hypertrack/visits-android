package com.hypertrack.android.view_models

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.ui.screens.splash_screen.SplashScreenFragmentDirections
import com.hypertrack.android.utils.CrashReportsProvider
import kotlinx.coroutines.launch

class SplashScreenViewModel(
    private val driverRepository: DriverRepository,
    private val accountRepository: AccountRepository,
    private val crashReportsProvider: CrashReportsProvider,
    private val permissionsInteractor: PermissionsInteractor
) : ViewModel() {

    val destination = MutableLiveData<NavDirections>()

    val loadingState = MutableLiveData<Boolean>()

    private fun proceedToLogin(activity: Activity) = when {
        driverRepository.hasDriverId -> {
            // already logged in
            crashReportsProvider.setUserIdentifier(driverRepository.driverId)
            when (permissionsInteractor.checkPermissionState(activity).getDestination()) {
                PermissionDestination.PASS -> {
                    destination.postValue(SplashScreenFragmentDirections.actionGlobalVisitManagementFragment())
                }
                PermissionDestination.FOREGROUND_AND_TRACKING,
                PermissionDestination.WHITELISTING,
                PermissionDestination.BACKGROUND -> {
                    destination.postValue(SplashScreenFragmentDirections.actionSplashScreenFragmentToPermissionRequestFragment())
                }
            }
        }
        accountRepository.isVerifiedAccount -> {
            // publishable key already verified
            destination.postValue(SplashScreenFragmentDirections.actionSplashScreenFragmentToDriverIdInputFragment())
        }
        else -> {
            // Log.d(TAG, "No publishable key found")
            destination.postValue(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())
        }
    }

    fun handleDeeplink(parameters: Map<String, Any>, activity: Activity) {
        // Log.d(TAG, "Got deeplink result $parameters")

        // Here we can inject obligatory input (publishable key and driver id)
        // as well as configuration parameters:
        //                  show_manual_visits (default false)
        //                  auto_check_in (default true)
        val key = parameters["publishable_key"] as String?
        val email = parameters["email"] as String?
        val driverId = parameters["driver_id"] as String?
        val showCheckIn = parameters["show_manual_visits"] as String? ?: ""
        val pickUpAllowed = parameters["pick_up_allowed"] as String? ?: ""
        // Log.v(TAG, "Got email $email, pk $key, driverId, $driverId, showCheckIn $showCheckIn, auto checking $autoCheckIn pickUp allowed $pickUpAllowed")
        if (key != null) {
            // Log.d(TAG, "Got key $key")
            try {
                loadingState.postValue(true)
                viewModelScope.launch {
                    val correctKey = accountRepository.onKeyReceived(
                        key,
                        checkInEnabled = showCheckIn,
                        pickUpAllowed = pickUpAllowed
                    )
                    // Log.d(TAG, "onKeyReceived finished")
                    if (correctKey) {
                        // Log.d(TAG, "Key validated successfully")
                        driverId?.let { driverRepository.driverId = it }
                        email?.let { driverRepository.driverId = it }
                        destination.postValue(SplashScreenFragmentDirections.actionSplashScreenFragmentToDriverIdInputFragment())
                    } else {
                        proceedToLogin(activity)
                    }
                }
                // Log.d(TAG, "coroutine finished")
            } catch (e: Throwable) {
                Log.w(TAG, "Cannot validate the key", e)
                proceedToLogin(activity)
            }
        } else {
            parameters["error"]?.let { Log.e(TAG, "Deeplink processing failed. $it") }
            proceedToLogin(activity)
        }
    }

    companion object {
        const val TAG = "SplashScreenVM"
    }

}
