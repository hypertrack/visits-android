package com.hypertrack.android.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

object PermissionsUtils {

    fun hasRequiredPermissions(): Boolean {
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

    fun hasPermission(permission: String) =
        MyApplication.context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

}