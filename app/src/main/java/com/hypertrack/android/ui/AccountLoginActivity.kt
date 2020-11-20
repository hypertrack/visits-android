package com.hypertrack.android.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.hypertrack.android.navigateTo
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.AccountLoginViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_login.*


class AccountLoginActivity : ProgressDialogActivity() {

    private val accountLoginViewModel: AccountLoginViewModel by viewModels {
        (application as MyApplication).injector.provideAccountLoginViewModelFactory(applicationContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {accountLoginViewModel.onLoginTextChanged(it)}
            }
        })

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {accountLoginViewModel.onPasswordTextChanged(it)}
            }
        })

        accountLoginViewModel.destination.observe(this) { navigateTo(it) }
        accountLoginViewModel.showToast.observe(this) { show ->
            Log.d(TAG, "show toast $show")
            if (show) Toast
                .makeText(this, getString(R.string.account_login_error_message), Toast.LENGTH_LONG)
                .show()
        }

        btnLogIn.setOnClickListener { accountLoginViewModel.onLoginClick() }

        accountLoginViewModel.showProgress.observe(this) { show ->
            Log.d(TAG, "show progress $show")
            if (show) showProgress() else dismissProgress()
        }

        accountLoginViewModel.isLoginButtonClickable.observe(this,) { isClickable ->
            Log.d(TAG, "Setting login button clickability $isClickable")
            btnLogIn.isEnabled = isClickable
            btnLogIn.setBackgroundColor(
                if (isClickable)
                    getColor(R.color.colorHyperTrackGreen)
                else
                    getColor(R.color.colorBtnDisable))

        }

    }

    companion object { const val TAG = "AccountLoginAct" }

}