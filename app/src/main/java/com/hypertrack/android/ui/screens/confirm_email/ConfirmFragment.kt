package com.hypertrack.android.ui.screens.confirm_email

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_confirm.*

class ConfirmFragment : ProgressDialogFragment(R.layout.fragment_confirm) {

    private val vm: ConfirmEmailViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadingState.observe(viewLifecycleOwner, {
            if (it) showProgress() else dismissProgress()
        })

        vm.errorText.observe(viewLifecycleOwner, {
            SnackbarUtil.showErrorSnackbar(view, it)
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        verified.setOnClickListener {
            vm.onVerifiedClick()
        }
        resend.setOnClickListener {
            vm.onResendClick()
        }
    }


}