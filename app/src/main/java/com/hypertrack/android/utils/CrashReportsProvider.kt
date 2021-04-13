package com.hypertrack.android.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface CrashReportsProvider {
    fun logException(e: Throwable, metadata: Map<String, String> = mapOf())
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
}