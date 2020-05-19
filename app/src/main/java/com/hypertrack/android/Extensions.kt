package com.hypertrack.android

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.ui.*
import com.hypertrack.android.utils.Destination
import com.hypertrack.logistics.android.github.R


private var dialog: Dialog? = null

val pass: Unit = Unit

// Show Progress Bar on anywhere
fun Context.showProgressBar() {

    val newDialog = dialog ?: Dialog(this)

    newDialog.setCancelable(false)
    newDialog.setContentView(R.layout.dialog_progress_bar)
    newDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    newDialog.show()

    dialog = newDialog

}

// Dismiss Progress bar
fun dismissProgressBar() = dialog?.dismiss()


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
    finish()

}
