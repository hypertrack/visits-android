package com.hypertrack.android.ui.screens.driver_id_input

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SimpleTextWatcher
import com.hypertrack.android.ui.common.textString
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_driver_id_input.*

class DriverIdInputFragment : ProgressDialogFragment(R.layout.fragment_driver_id_input) {

    private val vm: DriverLoginViewModel by viewModels {
        MyApplication.injector.provideUserScopeViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadingState.observe(viewLifecycleOwner, {
            if(it) {
                showProgress()
                displayLoginButtonEnabledState(false)
            } else {
                dismissProgress()
            }
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        etDriverId.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterChanged(text: String) {
                if (text.isNotBlank()) {
                    displayLoginButtonEnabledState(true)
                } else {
                    displayLoginButtonEnabledState(false)
                }
            }
        })

        btnCheckIn.setOnClickListener {
            vm.onLoginClick(etDriverId.textString(), mainActivity())
            btnCheckIn.isEnabled = false
        }

        vm.checkAutoLogin(mainActivity())
    }

    private fun displayLoginButtonEnabledState(enabled: Boolean) {
        btnCheckIn.isEnabled = enabled
        btnCheckIn.background = ContextCompat.getDrawable(requireContext(),
                if (enabled) R.drawable.bg_button
                else R.drawable.bg_button_disabled
        )
    }

    companion object {
        const val TAG = "LoginAct"
    }
}