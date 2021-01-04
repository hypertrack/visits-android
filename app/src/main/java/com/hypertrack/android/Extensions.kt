package com.hypertrack.android

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.ui.*
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.Destination
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream


val pass: Unit = Unit

private const val TAG = "Extensions"

fun AppCompatActivity.navigateTo(destination: Destination) {

    // Log.d(TAG, "Navigating to $destination")

    val targetActivity = when (destination) {
        Destination.SPLASH_SCREEN -> SplashScreen::class.java
        Destination.DRIVER_ID_INPUT -> DriverIdInputActivity::class.java
        Destination.PERMISSION_REQUEST -> PermissionRequestActivity::class.java
        Destination.VISITS_MANAGEMENT -> VisitsManagementActivity::class.java
        Destination.VISIT_DETAILS -> VisitDetailsActivity::class.java
        Destination.LOGIN -> AccountLoginActivity::class.java
    }

    if (javaClass == targetActivity) {
        // Log.d(TAG, "Destination $destination is current activity")
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

fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val result = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    // Log.v(TAG, "Encoded image $result")
    return result
}

fun String.decodeBase64Bitmap(): Bitmap {
    // Log.v(TAG, "decoding image $this")
    val decodedBytes = Base64.decode(this, Base64.NO_WRAP)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

// Retry policy is defined below and implemented at application level. Applied to image upload only.
suspend fun <T> retryWithBackoff(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 1000, //  1 sec
    maxDelay: Long = 100000,    // 100 secs
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (_: Throwable) {
            // NOOP
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}