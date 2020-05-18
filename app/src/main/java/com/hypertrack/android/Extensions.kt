package com.hypertrack.android

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.repository.Address
import com.hypertrack.android.ui.CheckInActivity
import com.hypertrack.android.ui.DeliveryDetailActivity
import com.hypertrack.android.ui.DeliveryListActivity
import com.hypertrack.android.ui.SplashScreen
import com.hypertrack.android.utils.Destination
import com.hypertrack.logistics.android.github.R


private var dialog: Dialog? = null

const val DELIVERY_UPDATE_RESULT_CODE = 4

const val KEY_EXTRA_DELIVERY_ID = "delivery_id"

const val BASE_URL = "https://live-app-backend.htprod.hypertrack.com/"
const val AUTH_HEADER_KEY = "Authorization"
const val AUTH_URL = "https://live-api.htprod.hypertrack.com/authenticate"

// Show Progress Bar on anywhere
fun Context.showProgressBar() {

    val newDialog = dialog ?: Dialog(this)

    newDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    newDialog.setCancelable(false)
    newDialog.setContentView(R.layout.dialog_progress_bar)
    newDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    newDialog.show()

    dialog = newDialog

}

// Dismiss Progress bar
fun dismissProgressBar() = dialog?.dismiss()

// Create address from Delivery Object
fun createAddress(address: Address): String {

    return address.street.plus("\n").plus(address.city).plus(",").plus(address.country)
        .plus("-${address.postalCode}")

}


fun AppCompatActivity.navigateTo(destination: Destination) {

    Log.d("Ext", "Navigating to $destination")

    val targetActivity = when (destination) {
        Destination.SPLASH_SCREEN -> SplashScreen::class.java
        Destination.LOGIN -> CheckInActivity::class.java
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
