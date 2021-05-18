package com.hypertrack.android.ui.screens.permission_request

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.utils.HyperTrackService

class PermissionRequestViewModel(
    private val permissionsInteractor: PermissionsInteractor,
    private val hyperTrackService: HyperTrackService
) : BaseViewModel() {

    val showWhitelistingButton =
        MutableLiveData<Boolean>(!permissionsInteractor.isWhitelistingGranted())
    val showPermissionsButton = MutableLiveData<Boolean>(true)
    val showSkipButton = MutableLiveData<Boolean>(false)

    fun requestWhitelisting(activity: Activity) {
        permissionsInteractor.requestWhitelisting(activity)
        showWhitelistingButton.postValue(!permissionsInteractor.isWhitelistingGranted())
    }

    private fun onPermissionResult(activity: Activity) {
        permissionsInteractor.checkPermissionsState(activity).let {
            when (permissionsInteractor.checkPermissionsState(activity)
                .getNextPermissionRequest()) {
                PermissionDestination.FOREGROUND_AND_TRACKING -> {
                    showPermissionsButton.postValue(true)
                }
                PermissionDestination.BACKGROUND -> {
                    syncDeviceSettings()
                    destination.postValue(PermissionRequestFragmentDirections.actionGlobalBackgroundPermissionsFragment())
                }
                PermissionDestination.PASS -> {
                    syncDeviceSettings()
                    if (permissionsInteractor.isWhitelistingGranted()) {
                        destination.postValue(PermissionRequestFragmentDirections.actionGlobalVisitManagementFragment())
                    }
                }
            }

            showWhitelistingButton.postValue(!permissionsInteractor.isWhitelistingGranted())
            showPermissionsButton.postValue(!permissionsInteractor.isBasePermissionsGranted())
            showSkipButton.postValue(permissionsInteractor.isBasePermissionsGranted())
        }
    }

    fun onSkipClicked() {
        if (permissionsInteractor.isBackgroundLocationGranted()) {
            destination.postValue(PermissionRequestFragmentDirections.actionGlobalVisitManagementFragment())
        } else {
            destination.postValue(PermissionRequestFragmentDirections.actionGlobalBackgroundPermissionsFragment())
        }
    }


    fun requestPermissions(activity: Activity) {
        permissionsInteractor.requestRequiredPermissions(activity)
    }

    fun onResume(activity: Activity) {
        onPermissionResult(activity)
    }

    private fun syncDeviceSettings() {
        hyperTrackService.syncDeviceSettings()
    }

    companion object {
        const val TAG = "PermissionRequestVM"
    }

}
