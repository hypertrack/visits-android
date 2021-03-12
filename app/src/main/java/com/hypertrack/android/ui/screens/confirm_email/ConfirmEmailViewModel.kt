package com.hypertrack.android.ui.screens.confirm_email

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.interactors.*
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.common.stringFromResource
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.Exception

class ConfirmEmailViewModel(
    private val loginInteractor: LoginInteractor,
    private val osUtilsProvider: OsUtilsProvider,
) : BaseViewModel() {

    private lateinit var email: String

    val loadingState = MutableLiveData<Boolean>()
    val proceedButtonEnabled = MutableLiveData<Boolean>(false)
    val errorText = MutableLiveData<String>()
    val clipboardCode = SingleLiveEvent<String>()

    fun init(email: String) {
        this.email = email
    }

    fun onResume() {
        osUtilsProvider.getClipboardContents()?.let {
            if (it.matches(Regex("^[0-9]{6}\$"))) {
                clipboardCode.postValue(it)
            }
        }
    }

    fun onVerifiedClick(code: String, complete: Boolean) {
        if (complete) {
            loadingState.postValue(true)
            viewModelScope.launch {
                val res = loginInteractor.verifyByOtpCode(email = email, code = code)
                loadingState.postValue(false)
                when (res) {
                    is OtpSuccess -> {
                        destination.postValue(
                            ConfirmFragmentDirections.actionConfirmFragmentToSignInFragment(
                                email
                            )
                        )
                    }
                    is OtpWrongCode -> {
                        errorText.postValue(R.string.wrong_code.stringFromResource())
                    }
                    is OtpError -> {
                        errorText.postValue(res.exception.message)
                    }
                }
            }
        }
    }

    fun onResendClick() {
        loadingState.postValue(true)
        viewModelScope.launch {
            try {
                loginInteractor.resendEmailConfirmation(email)
            } catch (e: Exception) {
                errorText.postValue(R.string.unknown_error.stringFromResource())
            }
            loadingState.postValue(false)
        }
    }

    fun onCodeChanged(complete: Boolean) {
        proceedButtonEnabled.postValue(complete)
    }

}