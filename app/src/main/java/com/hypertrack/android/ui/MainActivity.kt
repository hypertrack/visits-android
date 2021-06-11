package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.hypertrack.android.ui.base.NavActivity
import com.hypertrack.android.ui.common.Tab
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.ui.screens.splash_screen.MainActivityViewModel
import com.hypertrack.android.utils.DeeplinkResultListener
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : NavActivity() {

    private val activityVm: MainActivityViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    private val deepLinkProcessor = MyApplication.injector.deeplinkProcessor
    private val crashReportsProvider = MyApplication.injector.crashReportsProvider

    override val layoutRes: Int = R.layout.activity_main

    override val navHostId: Int = R.id.navHost

    private val customFragmentFactory = Injector.getCustomFragmentFactory(MyApplication.context)

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = customFragmentFactory
        super.onCreate(savedInstanceState)
        tvMockMode.setGoneState(MyApplication.MOCK_MODE.not())

        activityVm.activityDestination.observe(this, {
            findNavController(R.id.navHost).navigate(it)
        })
    }

    override fun onStart() {
        super.onStart()
        activityVm.onStart(this, intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        activityVm.onNewIntent(this, intent)
    }

    override fun onResume() {
        super.onResume()
        inForeground = true
    }

    override fun onPause() {
        inForeground = false
        super.onPause()
    }

    override fun onDestinationChanged(destination: NavDestination) {
        crashReportsProvider.log("Destination changed: ${destination.label.toString()}")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getCurrentFragment().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getCurrentFragment().onActivityResult(requestCode, resultCode, data)
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

    companion object {
        var inForeground: Boolean = false

        const val KEY_TAB = "tab"
    }
}