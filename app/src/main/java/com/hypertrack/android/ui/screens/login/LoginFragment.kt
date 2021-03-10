package com.hypertrack.android.ui.screens.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.AccountLoginViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : ProgressDialogFragment(R.layout.fragment_login) {

    private val accountLoginViewModel: AccountLoginViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //todo task
        findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToSignInFragment(
                null
            )
        )

        loginInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { accountLoginViewModel.onLoginTextChanged(it) }
            }
        })

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { accountLoginViewModel.onPasswordTextChanged(it) }
            }
        })

        accountLoginViewModel.destination.observe(viewLifecycleOwner) {
            if (it == Destination.DRIVER_ID_INPUT) {
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToDriverIdInputFragment())
            }
        }
        accountLoginViewModel.showLoginFailureToast.observe(viewLifecycleOwner) { show ->
            // Log.d(TAG, "show toast $show")
            if (show) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.account_login_error_message),
                        Toast.LENGTH_LONG
                ).show()
                deeplinkHint.text = getString(R.string.check_login_credentials_hint)
            }
        }

        btnLogIn.setOnClickListener { accountLoginViewModel.onLoginClick() }

        accountLoginViewModel.showProgress.observe(viewLifecycleOwner) { show ->
            // Log.d(TAG, "show progress $show")
            if (show) showProgress() else dismissProgress()
        }

        accountLoginViewModel.isLoginButtonClickable.observe(viewLifecycleOwner) { isClickable ->
            // Log.d(TAG, "Setting login button clickability $isClickable")
            btnLogIn.isEnabled = isClickable
            btnLogIn.setBackgroundColor(
                    if (isClickable)
                        requireContext().getColor(R.color.colorHyperTrackGreen)
                    else
                        requireContext().getColor(R.color.colorBtnDisable))

        }
    }

    companion object {
        const val TAG = "AccountLoginAct"
    }
}