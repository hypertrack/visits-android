package com.hypertrack.android.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.AccountLoginViewModel
import com.hypertrack.logistics.android.github.R


class AccountLoginActivity : ProgressDialogActivity() {

    private val accountLoginViewModel: AccountLoginViewModel by viewModels {
        (application as MyApplication).injector.provideAccountLoginViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


    }

    companion object { const val TAG = "LoginAct" }

}