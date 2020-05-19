package com.hypertrack.android.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.hypertrack.android.navigateTo
import com.hypertrack.android.pass
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.view_models.PermissionRequestViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.permission_request_activity.*


class PermissionRequestActivity : AppCompatActivity() {

    private val permissionRequestViewModel: PermissionRequestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.permission_request_activity)

        permissionRequestViewModel.destination.observe(this, Observer { destination ->
            when(destination) {
                Destination.PERMISSION_REQUEST -> pass
                else -> navigateTo(destination)
            }
        })

        btnContinue.setOnClickListener { permissionRequestViewModel.requestPermission(this) }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionRequestViewModel.onPermissionResult()
    }
}
