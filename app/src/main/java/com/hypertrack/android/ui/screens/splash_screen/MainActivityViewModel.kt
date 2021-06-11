package com.hypertrack.android.ui.screens.splash_screen

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.common.Tab
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.DeeplinkProcessor
import com.hypertrack.android.utils.DeeplinkResultListener
import com.hypertrack.logistics.android.github.NavGraphDirections
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val driverRepository: DriverRepository,
    private val accountRepository: AccountRepository,
    private val permissionsInteractor: PermissionsInteractor,
    private val deeplinkProcessor: DeeplinkProcessor,
    private val crashReportsProvider: CrashReportsProvider
) : BaseViewModel(), DeeplinkResultListener {

    val activityDestination = SingleLiveEvent<NavDirections>()
    val loadingState = MutableLiveData<Boolean>()

    private fun handleDeeplink(parameters: Map<String, Any>, activity: Activity) {
        activityDestination.postValue(NavGraphDirections.actionGlobalSplashScreenFragment())
//         Log.d(TAG, "Got deeplink result $parameters")
        val key = parameters["publishable_key"] as String?
        val email = parameters["email"] as String?
        val driverId = parameters["driver_id"] as String?
        val showCheckIn = (parameters["show_manual_visits"] as String?).toBoolean()
        val pickUpAllowed = (parameters["pick_up_allowed"] as String?).toBoolean()

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
                        if (driverRepository.hasDriverId) {
                            proceedToVisitsManagement(activity)
                        } else {
                            activityDestination.postValue(SplashScreenFragmentDirections.actionSplashScreenFragmentToDriverIdInputFragment())
                        }
                    } else {
                        proceedIfLoggedIn(activity)
                    }
                }
                // Log.d(TAG, "coroutine finished")
            } catch (e: Throwable) {
                Log.e(TAG, "Cannot validate the key", e)
                proceedIfLoggedIn(activity)
            }
        } else {
            parameters["error"]?.let {
                Log.e(TAG, "Deeplink processing failed. $it")
            }
            proceedIfLoggedIn(activity)
        }
    }

    private fun proceedIfLoggedIn(activity: Activity, tab: Tab? = null) = when {
        driverRepository.hasDriverId -> {
            // already logged in
            proceedToVisitsManagement(activity, tab)
        }
        accountRepository.isLoggedIn -> {
            // already logged in but doesn't have driver id
            activityDestination.postValue(SplashScreenFragmentDirections.actionSplashScreenFragmentToDriverIdInputFragment())
        }
        else -> {
            // not logged in
            // Log.d(TAG, "No publishable key found")
            activityDestination.postValue(
                SplashScreenFragmentDirections.actionSplashScreenFragmentToSignUpFragment()
            )
        }
    }

    private fun proceedToVisitsManagement(activity: Activity, tab: Tab? = null) {
        when (permissionsInteractor.checkPermissionsState(activity)
            .getNextPermissionRequest()) {
            PermissionDestination.PASS -> {
                activityDestination.postValue(
                    SplashScreenFragmentDirections.actionGlobalVisitManagementFragment(
                        tab
                    )
                )
            }
            PermissionDestination.FOREGROUND_AND_TRACKING -> {
                activityDestination.postValue(SplashScreenFragmentDirections.actionGlobalPermissionRequestFragment())
            }
            PermissionDestination.BACKGROUND -> {
                activityDestination.postValue(SplashScreenFragmentDirections.actionGlobalBackgroundPermissionsFragment())
            }
        }
    }

    fun onStart(activity: Activity, intent: Intent?) {
        val tab = intent?.getParcelableExtra<Tab>(MainActivity.KEY_TAB)
        if (tab != null) {
            proceedIfLoggedIn(activity, tab)
        } else {
            deeplinkProcessor.activityOnStart(activity, intent, this)
        }
    }

    fun onNewIntent(activity: Activity, intent: Intent?) {
        val tab = intent?.getParcelableExtra<Tab>(MainActivity.KEY_TAB)
        if (tab != null) {
            proceedIfLoggedIn(activity, tab)
        } else {
            deeplinkProcessor.activityOnNewIntent(activity, intent, this)
        }
    }

    override fun onDeeplinkResult(activity: Activity, parameters: Map<String, Any>) {
        handleDeeplink(parameters, activity)
    }

    private fun String?.toBoolean(): Boolean? {
        return when (this) {
            "False", "false" -> false
            "true", "True" -> true
            "", null -> null
            else -> null
        }
    }

    companion object {
        const val TAG = "SplashScreenVM"
    }
}
