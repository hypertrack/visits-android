package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.AccountLoginProvider

class AccountLoginViewModel(
    private val loginProvider: AccountLoginProvider,
    private val accountRepository: AccountRepository
) : ViewModel() {

    companion object { const val TAG = "AccountLoginVM" }
}