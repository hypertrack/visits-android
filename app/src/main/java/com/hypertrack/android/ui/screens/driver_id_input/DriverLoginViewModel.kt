package com.hypertrack.android.ui.screens.driver_id_input

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.utils.HyperTrackService

class DriverLoginViewModel(
    private val driverRepository: DriverRepository,
    private val hyperTrackService: HyperTrackService,
    private val permissionsInteractor: PermissionsInteractor,
) : ViewModel() {

    val loadingState = MutableLiveData<Boolean>()
    val destination = MutableLiveData<NavDirections>()

    fun onLoginClick(driverId: String?, activity: Activity) {
        driverId?.let {
            loadingState.postValue(true)
            // Log.d(TAG, "Proceeding with Driver Id $driverId")
            hyperTrackService.driverId = driverId
            driverRepository.driverId = driverId
            when (permissionsInteractor.checkPermissionsState(activity)
                .getNextPermissionRequest()) {
                PermissionDestination.PASS -> {
                    destination.postValue(DriverIdInputFragmentDirections.actionDriverIdInputFragmentToVisitManagementFragment())
                }
                PermissionDestination.FOREGROUND_AND_TRACKING,
                PermissionDestination.BACKGROUND -> {
                    destination.postValue(DriverIdInputFragmentDirections.actionDriverIdInputFragmentToPermissionRequestFragment())
                }
            }

        }
    }

    fun checkAutoLogin(activity: Activity) {
        // Log.v(TAG, "checkAutoLogin")
        if (driverRepository.hasDriverId) {
            onLoginClick(driverRepository.driverId, activity)
        }
    }

    companion object {
        const val TAG = "LoginVM"
    }
}