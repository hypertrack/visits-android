package com.hypertrack.android.view_models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.Destination
import com.judemanutd.autostarter.AutoStartPermissionHelper

class PermissionRequestViewModel(
    private val accountRepository: AccountRepository,
    private val context: Context
    ) : ViewModel() {

    private val autostarter = AutoStartPermissionHelper.getInstance()

    private val _destination = MutableLiveData(currentDestination())
    private val _whitelistingPromptVisibility = MutableLiveData(isWhitelistingApplicable())

    val destination: LiveData<Destination>
        get() = _destination
    val whitelistingPromptVisibility: LiveData<Boolean>
        get() = _whitelistingPromptVisibility


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
        Log.d(TAG, "AutoStart granted value is $granted" )
        accountRepository.wasWhitelisted = granted
        _whitelistingPromptVisibility.postValue(isWhitelistingApplicable())
    }

    fun onPermissionResult() {
        _destination.postValue(currentDestination())
        _whitelistingPromptVisibility.postValue(isWhitelistingApplicable())
    }

    private fun currentDestination() =
        if (hasRequiredPermissions()) Destination.VISITS_MANAGEMENT else Destination.PERMISSION_REQUEST

    private fun isWhitelistingApplicable() : Boolean {
        return autostarter.isAutoStartPermissionAvailable(context) && !accountRepository.wasWhitelisted
    }

    private fun hasRequiredPermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                    && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            -> true
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    && hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            -> true
            else -> false
        }
    }

    private fun hasPermission(permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    companion object { const val TAG = "PermissionRequestVM" }
}
