package com.hypertrack.android.ui.screens.sign_in

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_signin.*

class SignInFragment : ProgressDialogFragment(R.layout.fragment_signin) {

    //todo deeplinkHint

    private val vm: SignInViewModel by viewModels {
        MyApplication.injector.provideViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email_address.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { vm.onLoginTextChanged(it) }
            }
        })

        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { vm.onPasswordTextChanged(it) }
            }
        })

        vm.destination.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        vm.errorText.observe(viewLifecycleOwner, {
            incorrect.text = it
        })


        sign_in.setOnClickListener { vm.onLoginClick() }

        vm.showProgress.observe(viewLifecycleOwner) { show ->
            if (show) showProgress() else dismissProgress()
        }

        //todo rounded corners
        vm.isLoginButtonClickable.observe(viewLifecycleOwner) { isClickable ->
            // Log.d(TAG, "Setting login button clickability $isClickable")
            sign_in.isEnabled = isClickable
            sign_in.setBackgroundColor(
                if (isClickable)
                    requireContext().getColor(R.color.colorHyperTrackGreen)
                else
                    requireContext().getColor(R.color.colorBtnDisable)
            )
        }

        //todo task
        email_address.setText("qik51383@cuoly.com")
//        email_address.setText("spqrta@gmail.com")
        password.setText("qwerty123")
    }

    companion object {
        const val TAG = "AccountLoginAct"
    }
}