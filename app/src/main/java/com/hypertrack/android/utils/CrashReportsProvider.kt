package com.hypertrack.android.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface CrashReportsProvider {
    fun logException(e: Throwable, metadata: Map<String, String> = mapOf())
    fun log(txt: String)
    fun setCustomKey(key: String, value: String)
    fun setUserIdentifier(id: String)
}

class FirebaseCrashReportsProvider : CrashReportsProvider {
    override fun logException(e: Throwable, metadata: Map<String, String>) {
        if (
            e !is HttpException
            && e !is SocketTimeoutException
            && e !is UnknownHostException
            && e !is ConnectException
        ) {
            metadata.forEach {
                FirebaseCrashlytics.getInstance().setCustomKey(it.key, it.value)
            }
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun setUserIdentifier(id: String) = FirebaseCrashlytics.getInstance().setUserId(id)

    override fun setCustomKey(key: String, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    override fun log(txt: String) {
        FirebaseCrashlytics.getInstance().log(txt)
    }

    companion object {
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_PUBLISHABLE_KEY = "publishable_key"
    }
}

@JsonClass(generateAdapter = true)
class UserIdentifier(
    val deviceId: String,
    val driverId: String,
    val pubKey: String,
)

object ManuallyTriggeredException : Exception()