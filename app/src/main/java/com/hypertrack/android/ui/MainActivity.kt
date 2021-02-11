package com.hypertrack.android.ui

import android.content.Intent
import androidx.activity.viewModels
import com.hypertrack.android.ui.base.NavActivity
import com.hypertrack.android.ui.screens.permission_request.PermissionRequestFragment
import com.hypertrack.android.utils.DeeplinkResultListener
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.SplashScreenViewModel
import com.hypertrack.logistics.android.github.R

class MainActivity: NavActivity(), DeeplinkResultListener {

    val splashScreenViewModel: SplashScreenViewModel by viewModels {
        MyApplication.injector.provideSplashScreenViewModelFactory(MyApplication.context)
    }

    private val deepLinkProcessor = MyApplication.injector.deeplinkProcessor

    override val layoutRes: Int = R.layout.activity_main

    override val navHostId: Int = R.id.navHost

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        deepLinkProcessor.activityOnStart(this, intent, this)
    }

    override fun onStart() {
        super.onStart()
        deepLinkProcessor.activityOnNewIntent(this, intent, this)
    }

    override fun onDeeplinkResult(parameters: Map<String, Any>) {
        splashScreenViewModel.handleDeeplink(parameters)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getCurrentFragment().let {
            if(it is PermissionRequestFragment) {
                it.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}