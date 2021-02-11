package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.hypertrack.android.navigateTo
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.SplashScreenViewModel
import com.hypertrack.logistics.android.github.R

@Deprecated("")
class SplashScreen : ProgressDialogActivity() {

    private val splashScreenViewModel: SplashScreenViewModel by viewModels {
        MyApplication.injector.provideSplashScreenViewModelFactory(applicationContext)
    }

    private val deepLinkProcessor = Injector.deeplinkProcessor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_layout)

        splashScreenViewModel.destination
            .observe(this, { destination -> navigateTo(destination) })

    }

    override fun onStart() {
        super.onStart()
        // Log.d(TAG, "onStart")
//        splashScreenViewModel.loadingState
//            .observe(this, { show ->
//                if (show) showProgress() else dismissProgress()
//            })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Log.d(TAG, "onNewIntent")
    }

    companion object { const val TAG = "SplashScreen" }
}