package com.hypertrack.android.interactors

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.MyApplication
import com.judemanutd.autostarter.AutoStartPermissionHelper

interface PermissionsInteractor {
    fun checkPermissionState(activity: Activity): PermissionState
    fun isWhitelistingApplicable(): Boolean
    fun requestWhitelisting(activity: Activity)
    fun requestRequiredPermissions(activity: Activity)
    fun requestBackgroundLocationPermission(activity: Activity)
}

class PermissionsInteractorImpl(
    private val accountRepository: AccountRepository
) : PermissionsInteractor {

    private val autostarter = AutoStartPermissionHelper.getInstance()

    override fun checkPermissionState(activity: Activity): PermissionState {
        return PermissionState(
            activityTrackingGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                true
            },
            foregroundLocationGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
            backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            },
            whitelistingGranted = true,
        )
    }

    override fun isWhitelistingApplicable(): Boolean {
        return autostarter.isAutoStartPermissionAvailable(MyApplication.context) && !accountRepository.wasWhitelisted
    }

    override fun requestWhitelisting(activity: Activity) {
        val granted = AutoStartPermissionHelper.getInstance().getAutoStartPermission(activity)
//        Log.d(PermissionRequestViewModel.TAG, "AutoStart granted value is $granted")
        accountRepository.wasWhitelisted = granted
    }

    override fun requestRequiredPermissions(activity: Activity) {
        val requiredPermissions: Array<String> =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            }
        activity.requestPermissions(requiredPermissions, 42)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun requestBackgroundLocationPermission(activity: Activity) {
        activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 42)
    }

    private fun hasPermission(permission: String) =
        MyApplication.context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

}

class PermissionState(
    val activityTrackingGranted: Boolean,
    val foregroundLocationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
    val whitelistingGranted: Boolean
) {
    fun getDestination(): PermissionDestination {
        return when {
            !foregroundLocationGranted || !activityTrackingGranted -> PermissionDestination.FOREGROUND_AND_TRACKING
            !backgroundLocationGranted -> PermissionDestination.BACKGROUND
            !whitelistingGranted -> PermissionDestination.WHITELISTING
            else -> PermissionDestination.PASS
        }
    }
}

enum class PermissionDestination {
    PASS, FOREGROUND_AND_TRACKING, BACKGROUND, WHITELISTING
}