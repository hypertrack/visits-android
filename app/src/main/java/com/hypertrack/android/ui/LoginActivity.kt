package com.hypertrack.android.ui

import android.os.Bundle
import com.hypertrack.logistics.android.github.R


class LoginActivity : ProgressDialogActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


    }

    companion object { const val TAG = "LoginAct" }

}