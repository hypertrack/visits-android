package com.hypertrack.android.ui.screens.permission_request

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.utils.HyperTrackService

class PermissionRequestViewModel(
    private val permissionsInteractor: PermissionsInteractor,
    private val hyperTrackService: HyperTrackService
) : BaseViewModel() {

    val showPermissionsButton = MutableLiveData(true)
    val showSkipButton = MutableLiveData(false)

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
                    destination.postValue(PermissionRequestFragmentDirections.actionGlobalVisitManagementFragment())
                }
            }

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
