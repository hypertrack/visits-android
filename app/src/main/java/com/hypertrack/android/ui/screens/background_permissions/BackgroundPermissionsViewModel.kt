package com.hypertrack.android.ui.screens.background_permissions

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.ui.base.BaseViewModel

class BackgroundPermissionsViewModel(
    private val permissionsInteractor: PermissionsInteractor
) : BaseViewModel() {

    fun onAllowClick(activity: Activity) {
        permissionsInteractor.requestBackgroundLocationPermission(activity)
    }

    fun onPermissionResult(activity: Activity) {
        when (permissionsInteractor.checkPermissionsState(activity).getNextPermissionRequest()) {
            PermissionDestination.PASS -> {
                destination.postValue(BackgroundPermissionsFragmentDirections.actionGlobalVisitManagementFragment())
            }
            PermissionDestination.FOREGROUND_AND_TRACKING -> {
                destination.postValue(BackgroundPermissionsFragmentDirections.actionGlobalPermissionRequestFragment())
            }
            PermissionDestination.BACKGROUND -> {
                destination.postValue(BackgroundPermissionsFragmentDirections.actionGlobalVisitManagementFragment())
            }
        }

    }

}