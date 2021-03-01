package com.hypertrack.android.ui.screens.background_permissions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.MainActivity
import com.hypertrack.android.ui.base.BaseFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_background_permission.*

class BackgroundPermissionsFragment : BaseFragment<MainActivity>(R.layout.fragment_background_permission) {

    private val vm: BackgroundPermissionsViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        btnContinue.setOnClickListener {
            vm.onAllowClick(mainActivity())
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        vm.onPermissionResult(mainActivity())
    }
}