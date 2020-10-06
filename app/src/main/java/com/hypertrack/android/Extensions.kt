package com.hypertrack.android

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.ui.*
import com.hypertrack.android.utils.Destination


val pass: Unit = Unit


fun AppCompatActivity.navigateTo(destination: Destination) {

    Log.d("Ext", "Navigating to $destination")

    val targetActivity = when (destination) {
        Destination.SPLASH_SCREEN -> SplashScreen::class.java
        Destination.LOGIN -> LoginActivity::class.java
        Destination.PERMISSION_REQUEST -> PermissionRequestActivity::class.java
        Destination.VISITS_MANAGEMENT -> VisitsManagementActivity::class.java
        Destination.VISIT_DETAILS -> VisitDetailsActivity::class.java
    }

    if (javaClass == targetActivity) {
        Log.d("EXT", "Destination $destination is current activity")
        return
    }

    startActivity(Intent(this, targetActivity))
    if (destination != Destination.VISIT_DETAILS)
        finish()

}

fun Map<String, Any>?.toNote(): String {
    if (this == null) return ""
    val result = StringBuilder()
    this
        .filter { (key, _) -> !key.startsWith("ht_") }
        .forEach { (key, value) -> result.append("$key: $value\n") }
    return result.toString().dropLast(1)
}