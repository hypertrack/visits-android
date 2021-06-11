package com.hypertrack.android.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
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
    fun onDeeplinkResult(activity: Activity, parameters: Map<String, Any>)
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
//        Log.v("hypertrack-verbose", "activityOnStart ${intent?.data}")
        intent?.let {
            try {
                Branch.sessionBuilder(activity)
                    .withCallback(Branch2ResultListenerAdapter(activity, resultListener))
                    .withData(intent.data)
                    .init()
            } catch (e: Throwable) {
                crashReportsProvider.logException(e)
                resultListener.onDeeplinkResult(activity, mapOf("error" to e))
            }
        } ?: resultListener.onDeeplinkResult(activity, emptyMap())
    }

    override fun activityOnNewIntent(
            activity: Activity,
            intent: Intent?,
            resultListener: DeeplinkResultListener
    ) {
//        Log.v("hypertrack-verbose", "activityOnNewIntent ${intent?.data}")
        intent?.let {
            intent.putExtra("branch_force_new_session", true)
            activity.intent = intent
            try {
                Branch.sessionBuilder(activity)
                    .withCallback(Branch2ResultListenerAdapter(activity, resultListener))
                        .withData(intent.data)
                        .reInit()
            } catch (e: Throwable) {
                crashReportsProvider.logException(e)
                resultListener.onDeeplinkResult(activity, mapOf("error" to e))
            }
        }
    }

}

private class Branch2ResultListenerAdapter(
    private val activity: Activity,
    private val deeplinkResultListener: DeeplinkResultListener
) : Branch.BranchUniversalReferralInitListener {
    override fun onInitFinished(
        branchUniversalObject: BranchUniversalObject?,
        linkProperties: LinkProperties?,
        error: BranchError?
    ) =
        deeplinkResultListener.onDeeplinkResult(
            activity,
            branchUniversalObject?.contentMetadata?.customMetadata ?: emptyMap()
        )
}