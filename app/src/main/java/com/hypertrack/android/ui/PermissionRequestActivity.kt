package com.hypertrack.android.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.navigateTo
import com.hypertrack.android.pass
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.PermissionRequestViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.permission_request_activity.*


class PermissionRequestActivity : AppCompatActivity() {

    private val permissionRequestViewModel: PermissionRequestViewModel by viewModels {
        (application as MyApplication).injector.providePermissionRequestsViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.permission_request_activity)

        permissionRequestViewModel.destination.observe(this) { destination ->
            when(destination) {
                Destination.PERMISSION_REQUEST -> pass
                else -> navigateTo(destination)
            }
        }

        permissionRequestViewModel.whitelistingPromptVisibility.observe(this) { visible ->
            listOf<View>(btnWhitelisting, whitelistingMessage)
                .forEach { it.visibility = if(visible) View.VISIBLE else View.GONE }

        }

        btnContinue.setOnClickListener { permissionRequestViewModel.requestPermission(this) }

        btnWhitelisting.setOnClickListener { permissionRequestViewModel.requestWhitelisting(this) }

    }

    override fun onResume() {
        super.onResume()
        Log.d("PermissionRequestAct", "OnResume")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionRequestViewModel.onPermissionResult()
    }


}
