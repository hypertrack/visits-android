package com.hypertrack.android.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface CrashReportsProvider {
    fun logException(e: Throwable)
    fun setUserIdentifier(id: String)
}

class FirebaseCrashReportsProvider : CrashReportsProvider {
    override fun logException(e: Throwable) {
        if (
            e !is HttpException
            && e !is SocketTimeoutException
            && e !is UnknownHostException
            && e !is ConnectException
        ) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun setUserIdentifier(id: String) = FirebaseCrashlytics.getInstance().setUserId(id)
}