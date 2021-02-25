package com.hypertrack.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream


val pass: Unit = Unit

private const val TAG = "Extensions"

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
        retryParams: RetryParams = RetryParams(),
        block: suspend () -> T
): T {
    var currentDelay = retryParams.initialDelay
    repeat(retryParams.retryTimes) {
        try {
            return block()
        } catch (_: Throwable) {
            // NOOP
        }
        delay(currentDelay)
        currentDelay = (currentDelay * retryParams.factor).toLong().coerceAtMost(retryParams.maxDelay)
    }
    return block() // last attempt
}

class RetryParams(
        val retryTimes: Int = Int.MAX_VALUE,
        val initialDelay: Long = 1000, //  1 sec
        val maxDelay: Long = 100000,    // 100 secs
        val factor: Double = 2.0,
)