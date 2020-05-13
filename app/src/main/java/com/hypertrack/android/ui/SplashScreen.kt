package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.SplashScreenViewModel
import com.hypertrack.logistics.android.github.R
import io.branch.referral.Branch
import io.branch.referral.BranchError

class SplashScreen : AppCompatActivity() {


    private val splashScreenViewModel: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)

        val noPkFragment: TextView = findViewById(R.id.no_pk_fragment)
        val spinner: ProgressBar = findViewById(R.id.spinner)
        splashScreenViewModel
            .noAccountFragment.observe(this, Observer<Boolean> { value ->
                value?.let { show ->
                    noPkFragment.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        splashScreenViewModel.spinner
            .observe(this, Observer {it?.let {
                    show -> spinner.visibility = if (show) View.VISIBLE else View.GONE
            }  })
        splashScreenViewModel.destination
            .observe(this, Observer { it?.let { switchToActivity(it) } })

        splashScreenViewModel.login()

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        try {
            Branch.sessionBuilder(this)
                .withCallback(splashScreenViewModel).withData(intent?.data).init()
        } catch (e: Throwable) {
            Log.d(TAG, "Failed to initialize Branch IO")
            splashScreenViewModel.onInitFinished(null, BranchError(e.message, BranchError.ERR_BRANCH_INIT_FAILED))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")

        try {
            Branch.sessionBuilder(this).withCallback(splashScreenViewModel).reInit()
        } catch (e: Throwable) {
            Log.d(TAG, "Failed to re-init Branch IO")
            splashScreenViewModel.onInitFinished(null, BranchError(e.message, BranchError.ERR_BRANCH_INIT_FAILED))
        }
    }

    private fun switchToActivity(destination: Destination) {
        when (destination) {
            Destination.LOGIN -> navigateToActivity(CheckInActivity::class.java)
            Destination.LIST_VIEW -> navigateToActivity(ListActivity::class.java)
            else -> Log.d(TAG, "Unexpected destination $destination")
        }
    }

    private fun navigateToActivity(destination : Class<*>) {
        Log.i(TAG, "Navigating to $destination")
        startActivity(Intent(this@SplashScreen, destination))
        Log.d(TAG, "Finishing current activity")
        finish()
    }

    companion object {
        const val TAG = "SplashScreen"
    }
}