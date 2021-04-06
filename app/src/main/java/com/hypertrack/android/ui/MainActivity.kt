package com.hypertrack.android.ui

import android.content.Intent
import androidx.activity.viewModels
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.hypertrack.android.ui.base.NavActivity
import com.hypertrack.android.ui.screens.splash_screen.SplashScreenViewModel
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragment
import com.hypertrack.android.utils.DeeplinkResultListener
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.NavGraphDirections
import com.hypertrack.logistics.android.github.R

class MainActivity : NavActivity(), DeeplinkResultListener {

    val splashScreenViewModel: SplashScreenViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    private val deepLinkProcessor = MyApplication.injector.deeplinkProcessor

    override val layoutRes: Int = R.layout.activity_main

    override val navHostId: Int = R.id.navHost

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_SYNC) {
            if (getCurrentFragment() is VisitsManagementFragment) {
                //todo share vm
                (getCurrentFragment() as VisitsManagementFragment).refreshVisits()
            } else {
                findNavController(R.id.root).navigate(NavGraphDirections.actionGlobalVisitManagementFragment())
            }
        } else {
            deepLinkProcessor.activityOnNewIntent(this, intent, this)
        }
    }

    override fun onDestinationChanged(destination: NavDestination) {
        super.onDestinationChanged(destination)
    }

    override fun onStart() {
        super.onStart()
        deepLinkProcessor.activityOnStart(this, intent, this)
    }

    override fun onDeeplinkResult(parameters: Map<String, Any>) {
        splashScreenViewModel.handleDeeplink(parameters, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getCurrentFragment().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        if (getCurrentBaseFragment()?.onBackPressed() == false) {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}