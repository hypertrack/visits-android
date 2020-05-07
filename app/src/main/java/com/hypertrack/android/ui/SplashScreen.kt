package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hypertrack.android.AUTH_URL
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.repository.BasicAuthAccessTokenRepository
import com.hypertrack.android.utils.MyPreferences
import com.hypertrack.sdk.HyperTrack
import io.branch.referral.Branch
import io.branch.referral.BranchError
import org.json.JSONObject

class SplashScreen : AppCompatActivity(), Branch.BranchReferralInitListener {

    private val myPreferences: MyPreferences by lazy {
        MyPreferences(this@SplashScreen, Gson())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            myPreferences.restoreRepository() == null -> {
                Log.i(TAG, "No pk found, initializing Branch IO")
                return
            }
            myPreferences.getDriverValue() == null -> {
                Log.i(TAG, "No driver profile found. Navigating to checkin.")
                startActivity(Intent(this@SplashScreen, CheckInActivity::class.java))

            }
            else -> {
                Log.i(TAG, "Found existing driver profile")
                startActivity(Intent(this@SplashScreen, ListActivity::class.java))
            }
        }
        Log.d(TAG, "Finishing current activity")

        finish()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        Branch.sessionBuilder(this)
            .withCallback(this).withData(intent?.data).init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")
        Branch.sessionBuilder(this).withCallback(this).reInit()
    }

    override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
        Log.d(TAG, "Branch init finished with params $referringParams")
        val key = referringParams?.optString("publishable_key")!!
        if (key.isNotEmpty()) {
            Log.d(TAG, "Got key $key")
            val repo = BasicAuthAccessTokenRepository(
                AUTH_URL, HyperTrack.getInstance(this, key as String).deviceID, key
            )
            myPreferences.persistRepository(repo)
            return
        }

        error?.let { Log.e(TAG, "Branch IO init failed. $error") }

        showNoPkFragment()

    }

    private fun showNoPkFragment() {
        Log.e(TAG, "No publishable key")
    }


    companion object {
        const val TAG = "SplashScreen"
    }
}