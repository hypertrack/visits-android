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
        Destination.LOGIN -> CheckInActivity::class.java
        Destination.PERMISSION_REQUEST -> PermissionRequestActivity::class.java
        Destination.LIST_VIEW -> DeliveryListActivity::class.java
        Destination.DETAILS_VIEW -> DeliveryDetailActivity::class.java
    }

    if (javaClass == targetActivity) {
        Log.d("EXT", "Destination $destination is current activity")
        return
    }

    startActivity(Intent(this, targetActivity))
    if (destination != Destination.DETAILS_VIEW)
        finish()

}
