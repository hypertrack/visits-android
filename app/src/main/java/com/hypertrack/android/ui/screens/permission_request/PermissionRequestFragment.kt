package com.hypertrack.android.ui.screens.permission_request

import android.os.Bundle
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
        MyApplication.injector.provideUserScopeViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        vm.showSkipButton.observe(viewLifecycleOwner) { visible ->
            btnSkip.setGoneState(!visible)
        }

        vm.showPermissionsButton.observe(viewLifecycleOwner) { show ->
            btnAllow.setGoneState(!show)
        }

        btnSkip.setOnClickListener { vm.onSkipClicked() }
        btnAllow.setOnClickListener { vm.requestPermissions(mainActivity()) }

    }

    override fun onResume() {
        super.onResume()
        vm.onResume(mainActivity())
    }
}