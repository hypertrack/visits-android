package com.hypertrack.android.ui.screens.driver_id_input

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.utils.HyperTrackService

class DriverLoginViewModel(
    private val driverRepository: DriverRepository,
    private val hyperTrackService: HyperTrackService,
    private val permissionsInteractor: PermissionsInteractor,
) : BaseViewModel() {

    val loadingState = MutableLiveData<Boolean>()

    fun onLoginClick(driverId: String?, activity: Activity) {
        driverId?.let {
            loadingState.postValue(true)
            // Log.d(TAG, "Proceeding with Driver Id $driverId")
            driverRepository.driverId = driverId
            when (permissionsInteractor.checkPermissionsState(activity)
                .getNextPermissionRequest()) {
                PermissionDestination.PASS -> {
                    destination.postValue(DriverIdInputFragmentDirections.actionGlobalVisitManagementFragment())
                }
                PermissionDestination.FOREGROUND_AND_TRACKING -> {
                    destination.postValue(DriverIdInputFragmentDirections.actionGlobalPermissionRequestFragment())
                }
                PermissionDestination.BACKGROUND -> {
                    destination.postValue(DriverIdInputFragmentDirections.actionGlobalBackgroundPermissionsFragment())
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