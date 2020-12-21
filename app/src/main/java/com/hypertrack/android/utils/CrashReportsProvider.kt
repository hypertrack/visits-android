package com.hypertrack.android.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

interface CrashReportsProvider {
    fun logException(e: Throwable)
    fun setUserIdentifier(id: String)
}

class FirebaseCrashReportsProvider: CrashReportsProvider {
    override fun logException(e: Throwable) = FirebaseCrashlytics.getInstance().recordException(e)
    override fun setUserIdentifier(id: String) = FirebaseCrashlytics.getInstance().setUserId(id)
}