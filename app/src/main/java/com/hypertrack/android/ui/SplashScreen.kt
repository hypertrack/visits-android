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
import com.hypertrack.android.navigateTo
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
            .noAccountFragment.observe(this, Observer<Boolean> { show ->
                    noPkFragment.visibility = if (show) View.VISIBLE else View.GONE
            })
        splashScreenViewModel.spinner
            .observe(this, Observer {
                    show -> spinner.visibility = if (show) View.VISIBLE else View.GONE
            })
        splashScreenViewModel.destination
            .observe(this, Observer { destination -> navigateTo(destination) })

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

    companion object {
        const val TAG = "SplashScreen"
    }
}