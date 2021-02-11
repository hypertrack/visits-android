package com.hypertrack.android.ui.screens.driver_id_input

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.JustLoading
import com.hypertrack.android.ui.base.JustSuccess
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.SimpleTextWatcher
import com.hypertrack.android.utils.textString
import com.hypertrack.android.view_models.DriverLoginViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_driver_id_input.*

class DriverIdInputFragment: ProgressDialogFragment(R.layout.fragment_driver_id_input) {

    private val driverLoginModel: DriverLoginViewModel by viewModels {
        MyApplication.injector.provideDriverLoginViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        driverLoginModel.state.observe(viewLifecycleOwner, { state ->
            if(state !is JustLoading) {
                dismissProgress()
            }
            when(state) {
                is JustLoading -> {
                    showProgress()
                    displayLoginButtonEnabledState(false)
                }
                is JustSuccess -> {
                    //todo check if permissions granted and whitelisted
//                    if(PermissionsUtils.hasRequiredPermissions()) {
//                        findNavController().navigate(DriverIdInputFragmentDirections.actionDriverIdInputFragmentToVisitManagementFragment())
//                    } else {
                        findNavController().navigate(DriverIdInputFragmentDirections.actionDriverIdInputFragmentToPermissionRequestFragment())
//                    }
                }
            }
        })

        etDriverId.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterChanged(text: String) {
                if(text.isNotBlank()) {
                    displayLoginButtonEnabledState(true)
                } else {
                    displayLoginButtonEnabledState(false)
                }
            }
        })

        btnCheckIn.setOnClickListener {
            driverLoginModel.onLoginClick(etDriverId.textString())
            btnCheckIn.isEnabled = false
        }

        driverLoginModel.checkAutoLogin()
    }

    private fun displayLoginButtonEnabledState(enabled: Boolean) {
        btnCheckIn.isEnabled = enabled
        btnCheckIn.background = ContextCompat.getDrawable(requireContext(),
            if (enabled) R.drawable.bg_button
            else R.drawable.bg_button_disabled
        )
    }

    companion object { const val TAG = "LoginAct" }
}