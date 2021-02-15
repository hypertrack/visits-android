package com.hypertrack.android.ui.screens.permission_request

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.view_models.PermissionRequestViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_permission_request.*

class PermissionRequestFragment : ProgressDialogFragment(R.layout.fragment_permission_request) {

    private val permissionRequestViewModel: PermissionRequestViewModel by viewModels {
        MyApplication.injector.providePermissionRequestsViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionRequestViewModel.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                is PermissionRequestViewModel.PermissionsGranted -> {
                    findNavController().navigate(PermissionRequestFragmentDirections.actionPermissionRequestFragmentToVisitManagementFragment())
                }
                is PermissionRequestViewModel.PermissionsNotGranted -> {
                    permissionRequestViewModel.requestPermission(mainActivity())
                }
            }
        })

        permissionRequestViewModel.whitelistingRequired.observe(viewLifecycleOwner) { visible ->
            listOf<View>(btnWhitelisting, whitelistingMessage)
                .forEach { it.setGoneState(!visible) }
        }

        btnContinue.setOnClickListener { permissionRequestViewModel.requestPermission(mainActivity()) }

        btnWhitelisting.setOnClickListener {
            permissionRequestViewModel.requestWhitelisting(
                mainActivity()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("PermissionRequestAct", "OnResume")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionRequestViewModel.onPermissionResult()
    }


}