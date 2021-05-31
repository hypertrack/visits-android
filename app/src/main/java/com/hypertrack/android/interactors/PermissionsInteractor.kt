package com.hypertrack.android.interactors

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.MyApplication

interface PermissionsInteractor {
    fun checkPermissionsState(activity: Activity): PermissionsState
    fun requestWhitelisting(activity: Activity)
    fun requestRequiredPermissions(activity: Activity)
    fun requestBackgroundLocationPermission(activity: Activity)
    fun isWhitelistingGranted(): Boolean
    fun isBackgroundLocationGranted(): Boolean
    fun isBasePermissionsGranted(): Boolean
}

class PermissionsInteractorImpl(
    private val accountRepository: AccountRepository
) : PermissionsInteractor {


    override fun checkPermissionsState(activity: Activity): PermissionsState {
        return PermissionsState(
            activityTrackingGranted = isActivityGranted(),
            foregroundLocationGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
            backgroundLocationGranted = isBackgroundLocationGranted(),
        )
    }

    override fun isWhitelistingGranted() = true

    override fun isBasePermissionsGranted(): Boolean {
        return isActivityGranted() && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun isActivityGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            true
        }
    }

    override fun isBackgroundLocationGranted(): Boolean {
        // we don't need ACCESS_BACKGROUND_LOCATION before R see
        // https://hypertrack.com/docs/install-sdk-android/#what-permissions-are-required-for-the-sdk-to-work
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        }
    }

    override fun requestWhitelisting(activity: Activity) {
        accountRepository.wasWhitelisted = true
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

class PermissionsState(
    val activityTrackingGranted: Boolean,
    val foregroundLocationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
) {
    fun getNextPermissionRequest(): PermissionDestination {
        return when {
            !foregroundLocationGranted || !activityTrackingGranted -> PermissionDestination.FOREGROUND_AND_TRACKING
            !backgroundLocationGranted -> PermissionDestination.BACKGROUND
            else -> PermissionDestination.PASS
        }
    }
}

enum class PermissionDestination {
    PASS, FOREGROUND_AND_TRACKING, BACKGROUND
}