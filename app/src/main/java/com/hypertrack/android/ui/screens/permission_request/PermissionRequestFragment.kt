package com.hypertrack.android.ui.screens.permission_request

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_permission_request.*

class PermissionRequestFragment : ProgressDialogFragment(R.layout.fragment_permission_request) {

    private val vm: PermissionRequestViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        vm.whitelistingRequired.observe(viewLifecycleOwner) { visible ->
            listOf<View>(btnWhitelisting, whitelistingMessage)
                .forEach { it.setGoneState(!visible) }
        }

        vm.showPermissionsButton.observe(viewLifecycleOwner) { show ->
            listOf<View>(btnContinue, permissionRationalMessage)
                    .forEach { it.setGoneState(!show) }
        }

        btnContinue.setOnClickListener { vm.requestPermissions(mainActivity()) }

        btnWhitelisting.setOnClickListener {
            vm.requestWhitelisting(mainActivity())
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume(mainActivity())
    }
}