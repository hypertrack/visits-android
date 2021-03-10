package com.hypertrack.android.ui.screens.confirm_email

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.ui.views.VerificationCodeView
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_confirm.*


class ConfirmFragment : ProgressDialogFragment(R.layout.fragment_confirm) {

    private val args: ConfirmFragmentArgs by navArgs()

    private val vm: ConfirmEmailViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.init(args.email)

        vm.loadingState.observe(viewLifecycleOwner, {
            if (it) showProgress() else dismissProgress()
        })

        vm.proceedButtonEnabled.observe(viewLifecycleOwner, {
            verified.isSelected = it
            verified.isEnabled = it
        })

        vm.errorText.observe(viewLifecycleOwner, {
            SnackbarUtil.showErrorSnackbar(view, it)
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        verificationCode.listener = object : VerificationCodeView.VerificationCodeListener {
            override fun onCodeChanged(code: String, complete: Boolean) {
                vm.onCodeChanged(complete)
            }

            override fun onEnterPressed(complete: Boolean) {
                vm.onVerifiedClick(verificationCode.code, complete)
            }
        }
        verificationCode.etCode.requestFocus()
        val inputMethodManager =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(requireActivity().currentFocus, 0)

        verified.setOnClickListener {
            vm.onVerifiedClick(verificationCode.code, verificationCode.isCodeComplete)
        }
        resend.setOnClickListener {
            vm.onResendClick()
        }
    }


}