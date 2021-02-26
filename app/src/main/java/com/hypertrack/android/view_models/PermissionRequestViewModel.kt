package com.hypertrack.android.view_models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.ui.base.BaseStateViewModel
import com.hypertrack.android.ui.base.State
import com.hypertrack.android.ui.common.PermissionsUtils
import com.judemanutd.autostarter.AutoStartPermissionHelper

class PermissionRequestViewModel(
        private val accountRepository: AccountRepository,
        private val context: Context
) : BaseStateViewModel() {

    private val autostarter = AutoStartPermissionHelper.getInstance()

    val whitelistingRequired = MutableLiveData(isWhitelistingApplicable())

    fun requestPermission(activity: Activity) {
        val requiredPermissions: Array<String> =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                else arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACTIVITY_RECOGNITION
                )
        activity.requestPermissions(requiredPermissions, 42)
    }

    fun requestWhitelisting(activity: Activity) {
        val granted = AutoStartPermissionHelper.getInstance().getAutoStartPermission(activity)
        Log.d(TAG, "AutoStart granted value is $granted")
        accountRepository.wasWhitelisted = granted
        whitelistingRequired.postValue(isWhitelistingApplicable())
    }

    fun onPermissionResult() {
        if (PermissionsUtils.hasRequiredPermissions()) {
            state.postValue(PermissionsGranted)
        } else {
            state.postValue(PermissionsNotGranted)
        }
        whitelistingRequired.postValue(isWhitelistingApplicable())
    }

    private fun isWhitelistingApplicable(): Boolean {
        return autostarter.isAutoStartPermissionAvailable(context) && !accountRepository.wasWhitelisted
    }

    companion object {
        const val TAG = "PermissionRequestVM"
    }

    object PermissionsGranted : State()
    object PermissionsNotGranted : State()
}
