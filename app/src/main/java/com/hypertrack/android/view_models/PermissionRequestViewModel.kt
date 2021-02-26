package com.hypertrack.android.view_models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.interactors.PermissionDestination
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.BaseStateViewModel
import com.hypertrack.android.ui.base.State
import com.hypertrack.android.ui.screens.permission_request.PermissionRequestFragmentDirections
import com.judemanutd.autostarter.AutoStartPermissionHelper

class PermissionRequestViewModel(
    private val permissionsInteractor: PermissionsInteractor,
) : ViewModel() {

    val whitelistingRequired = MutableLiveData<Boolean>(permissionsInteractor.isWhitelistingApplicable())

    val destination = MutableLiveData<NavDirections>()

    fun requestWhitelisting(activity: Activity) {
        permissionsInteractor.requestWhitelisting(activity)
        whitelistingRequired.postValue(permissionsInteractor.isWhitelistingApplicable())
    }

    fun onPermissionResult(activity: Activity) {
        when(permissionsInteractor.checkPermissionState(activity).getDestination()) {
            PermissionDestination.PASS -> {
                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToVisitManagementFragment())
            }
            PermissionDestination.FOREGROUND_AND_TRACKING -> {}
            PermissionDestination.BACKGROUND -> {
                //todo TASK
                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToVisitManagementFragment())
//                destination.postValue(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToVisitManagementFragment())
            }
            PermissionDestination.WHITELISTING -> {}
        }

        whitelistingRequired.postValue(permissionsInteractor.isWhitelistingApplicable())
    }

    fun requestPermissions(activity: Activity) {
        permissionsInteractor.requestRequiredPermissions(activity)
    }

    companion object {
        const val TAG = "PermissionRequestVM"
    }

}
