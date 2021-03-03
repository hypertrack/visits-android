package com.hypertrack.android.ui.screens.background_permissions

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor

class BackgroundPermissionsViewModel(
        private val permissionsInteractor: PermissionsInteractor
) : ViewModel() {

    val destination = MutableLiveData<NavDirections>()

    fun onAllowClick(activity: Activity) {
        permissionsInteractor.requestBackgroundLocationPermission(activity)
    }

    fun onPermissionResult(activity: Activity) {
        when (permissionsInteractor.checkPermissionsState(activity).getNextPermissionRequest()) {
            PermissionDestination.PASS -> {
                destination.postValue(BackgroundPermissionsFragmentDirections.actionBackgroundPermissionsFragmentToVisitManagementFragment())
            }
            PermissionDestination.FOREGROUND_AND_TRACKING -> {
                destination.postValue(BackgroundPermissionsFragmentDirections.actionBackgroundPermissionsFragmentToPermissionRequestFragment())
            }
            PermissionDestination.BACKGROUND -> {
                destination.postValue(BackgroundPermissionsFragmentDirections.actionBackgroundPermissionsFragmentToVisitManagementFragment())
            }
        }

    }

}