package com.hypertrack.android.ui.screens.permission_request

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.utils.HyperTrackService

class PermissionRequestViewModel(
    private val permissionsInteractor: PermissionsInteractor,
    private val hyperTrackService: HyperTrackService
) : ViewModel() {

    val whitelistingRequired = MutableLiveData<Boolean>(!permissionsInteractor.isWhitelistingGranted())
    val showPermissionsButton = MutableLiveData<Boolean>(true)

    val destination = MutableLiveData<NavDirections>()

    fun requestWhitelisting(activity: Activity) {
        permissionsInteractor.requestWhitelisting(activity)
        whitelistingRequired.postValue(!permissionsInteractor.isWhitelistingGranted())
    }

    private fun onPermissionResult(activity: Activity) {
        when (permissionsInteractor.checkPermissionState(activity).getDestination()) {
            PermissionDestination.PASS -> {
                syncDeviceSettings()
                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToVisitManagementFragment())
            }
            PermissionDestination.FOREGROUND_AND_TRACKING -> {
                showPermissionsButton.postValue(true)
            }
            PermissionDestination.BACKGROUND -> {
                syncDeviceSettings()
                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToBackgroundPermissionsFragment())
            }
            PermissionDestination.WHITELISTING -> {
                syncDeviceSettings()
                showPermissionsButton.postValue(false)
            }
        }

        whitelistingRequired.postValue(!permissionsInteractor.isWhitelistingGranted())
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
