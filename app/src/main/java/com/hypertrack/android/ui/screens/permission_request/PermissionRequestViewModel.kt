package com.hypertrack.android.ui.screens.permission_request

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor

class PermissionRequestViewModel(
    private val permissionsInteractor: PermissionsInteractor,
) : ViewModel() {

    val whitelistingRequired = MutableLiveData<Boolean>(!permissionsInteractor.isWhitelistingGranted())
    val showPermissionsButton = MutableLiveData<Boolean>(true)

    val destination = MutableLiveData<NavDirections>()

    fun requestWhitelisting(activity: Activity) {
        permissionsInteractor.requestWhitelisting(activity)
        whitelistingRequired.postValue(!permissionsInteractor.isWhitelistingGranted())
    }

    fun onPermissionResult(activity: Activity) {
        when(permissionsInteractor.checkPermissionState(activity).getDestination()) {
            PermissionDestination.PASS -> {
                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToVisitManagementFragment())
            }
            PermissionDestination.FOREGROUND_AND_TRACKING -> {
                showPermissionsButton.postValue(true)
            }
            PermissionDestination.BACKGROUND -> {
                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToBackgroundPermissionsFragment())
            }
            PermissionDestination.WHITELISTING -> {
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

    companion object {
        const val TAG = "PermissionRequestVM"
    }

}
