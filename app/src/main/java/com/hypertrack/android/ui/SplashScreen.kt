package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.hypertrack.android.navigateTo
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.SplashScreenViewModel
import com.hypertrack.logistics.android.github.R

class SplashScreen : ProgressDialogActivity() {


    private val splashScreenViewModel: SplashScreenViewModel by viewModels {
        (application as MyApplication).injector.provideSplashScreenViewModelFactory(applicationContext)
    }

    private val deepLinkProcessor = Injector.deeplinkProcessor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)

        splashScreenViewModel
            .noAccountFragment.observe(this, { show ->
                    Log.d(TAG,"No pk fragment $show")
            })
        splashScreenViewModel.destination
            .observe(this, { destination -> navigateTo(destination) })

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        splashScreenViewModel.spinner
            .observe(this, { show ->
                if (show) showProgress() else dismissProgress()
            })

        deepLinkProcessor.activityOnStart(this, intent, splashScreenViewModel)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")

        deepLinkProcessor.activityOnNewIntent(this, intent, splashScreenViewModel)

    }

    companion object { const val TAG = "SplashScreen" }
}