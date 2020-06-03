package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.hypertrack.android.navigateTo
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.SplashScreenViewModel
import com.hypertrack.logistics.android.github.R
import io.branch.referral.Branch
import io.branch.referral.BranchError
import kotlinx.android.synthetic.main.splash_screen_layout.*

class SplashScreen : ProgressDialogActivity() {


    private val splashScreenViewModel: SplashScreenViewModel by viewModels {
        (application as MyApplication).injector.provideSplashScreenViewModelFactory(applicationContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)

        splashScreenViewModel
            .noAccountFragment.observe(this, Observer { show ->
                    noPkFragment.visibility = if (show) View.VISIBLE else View.GONE
            })
        splashScreenViewModel.destination
            .observe(this, Observer { destination -> navigateTo(destination) })

        splashScreenViewModel.login()

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        splashScreenViewModel.spinner
            .observe(this, Observer { show ->
                if (show) showProgress() else dismissProgress()
            })
        try {
            Branch.sessionBuilder(this)
                .withCallback(splashScreenViewModel).withData(intent?.data).init()
        } catch (e: Throwable) {
            Log.d(TAG, "Failed to initialize Branch IO")
            splashScreenViewModel.onInitFinished(null, null,
                BranchError(e.message, BranchError.ERR_BRANCH_INIT_FAILED))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")

        intent?.let {
            intent.putExtra("branch_force_new_session", true)
            setIntent(intent)
            try {
                Branch.sessionBuilder(this)
                    .withCallback(splashScreenViewModel)
                    .withData(intent.data)
                    .reInit()
            } catch (e: Throwable) {
                Log.d(TAG, "Failed to re-init Branch IO")
                splashScreenViewModel.onInitFinished(null, null,
                    BranchError(e.message, BranchError.ERR_BRANCH_INIT_FAILED))
            }
        }
    }



    companion object { const val TAG = "SplashScreen" }
}