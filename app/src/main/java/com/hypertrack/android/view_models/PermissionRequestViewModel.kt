package com.hypertrack.android.view_models

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.utils.Destination

class PermissionRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _destination = MutableLiveData(currentDestination())

    val destination: LiveData<Destination>
        get() = _destination


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

    fun onPermissionResult() = _destination.postValue(currentDestination())

    private fun currentDestination() =
        if (hasRequiredPermissions()) Destination.VISITS_MANAGEMENT else Destination.PERMISSION_REQUEST

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

}
