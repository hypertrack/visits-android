package com.hypertrack.android.ui.screens.confirm_email

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.ui.common.Utils
import com.hypertrack.android.ui.views.VerificationCodeView
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_confirm.*
import kotlinx.android.synthetic.main.fragment_login.*


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

        vm.clipboardCode.observe(viewLifecycleOwner, {
            verificationCode.code = it
            Toast.makeText(requireContext(), R.string.code_from_clipboard, LENGTH_SHORT).show()
            Utils.hideKeyboard(mainActivity())
        })

        tvEmail.text = args.email

        verificationCode.listener = object : VerificationCodeView.VerificationCodeListener {
            override fun onCodeChanged(code: String, complete: Boolean) {
                vm.onCodeChanged(complete)
            }

            override fun onEnterPressed(complete: Boolean) {
                vm.onVerifiedClick(verificationCode.code, complete)
            }
        }
        Utils.showKeyboard(mainActivity(), verificationCode.etCode)

        verified.setOnClickListener {
            vm.onVerifiedClick(verificationCode.code, verificationCode.isCodeComplete)
        }

        resend.setOnClickListener {
            vm.onResendClick()
        }

    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }
}