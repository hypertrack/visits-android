package com.hypertrack.android.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.LinkProperties

interface DeeplinkProcessor {
    fun appOnCreate(application: Application)
    fun activityOnStart(activity: Activity, intent: Intent?, resultListener: DeeplinkResultListener)
    fun activityOnNewIntent(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    )
}

interface DeeplinkResultListener {
    fun onDeeplinkResult(parameters: Map<String, Any>)
}

class BranchIoDeepLinkProcessor(
    private val crashReportsProvider: CrashReportsProvider
) : DeeplinkProcessor {

    override fun appOnCreate(application: Application) {
        //        Branch.enableLogging()
        Branch.getAutoInstance(application)
    }

    override fun activityOnStart(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    ) {
        intent?.let {
            try {
                Branch.sessionBuilder(activity)
                    .withCallback(Branch2ResultListenerAdapter(resultListener))
                    .withData(intent.data)
                    .init()
            } catch (e: Throwable) {
                crashReportsProvider.logException(e)
                resultListener.onDeeplinkResult(mapOf("error" to e))
            }
        } ?: resultListener.onDeeplinkResult(emptyMap())
    }

    override fun activityOnNewIntent(
        activity: Activity,
        intent: Intent?,
        resultListener: DeeplinkResultListener
    ) {
        intent?.let {
            intent.putExtra("branch_force_new_session", true)
            activity.intent = intent
            try {
                Branch.sessionBuilder(activity)
                    .withCallback(Branch2ResultListenerAdapter(resultListener))
                    .withData(intent.data)
                    .reInit()
            } catch (e: Throwable) {
                crashReportsProvider.logException(e)
                resultListener.onDeeplinkResult(mapOf("error" to e))
            }
        } ?: resultListener.onDeeplinkResult(emptyMap())
    }

}

private class Branch2ResultListenerAdapter(
    val deeplinkResultListener: DeeplinkResultListener
) : Branch.BranchUniversalReferralInitListener {
    override fun onInitFinished(
        branchUniversalObject: BranchUniversalObject?,
        linkProperties: LinkProperties?,
        error: BranchError?
    ) =
        deeplinkResultListener.onDeeplinkResult(
             branchUniversalObject?.contentMetadata?.customMetadata?: emptyMap()
         )
}