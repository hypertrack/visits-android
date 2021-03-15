package com.hypertrack.android.ui.screens.confirm_email

import android.app.Activity
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
    private val permissionsInteractor: PermissionsInteractor,
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

    fun onVerifiedClick(code: String, complete: Boolean, activity: Activity) {
        if (complete) {
            loadingState.postValue(true)
            viewModelScope.launch {
                val res = loginInteractor.verifyByOtpCode(email = email, code = code)
                loadingState.postValue(false)
                when (res) {
                    is OtpSuccess -> {
                        when (permissionsInteractor.checkPermissionsState(activity)
                            .getNextPermissionRequest()) {
                            PermissionDestination.PASS -> {
                                destination.postValue(ConfirmFragmentDirections.actionGlobalVisitManagementFragment())
                            }
                            PermissionDestination.FOREGROUND_AND_TRACKING -> {
                                destination.postValue(ConfirmFragmentDirections.actionGlobalPermissionRequestFragment())
                            }
                            PermissionDestination.BACKGROUND -> {
                                destination.postValue(ConfirmFragmentDirections.actionGlobalBackgroundPermissionsFragment())
                            }
                        }
                    }
                    is OtpSignInRequired -> {
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
            val res = loginInteractor.resendEmailConfirmation(email)
            loadingState.postValue(false)
            when (res) {
                ResendNoAction -> {
                    return@launch
                }
                ResendAlreadyConfirmed -> {
                    destination.postValue(
                        ConfirmFragmentDirections.actionConfirmFragmentToSignInFragment(
                            email
                        )
                    )
                }
                is ResendError -> {
                    errorText.postValue(res.exception.message)
                }
            }

        }
    }

    fun onCodeChanged(complete: Boolean) {
        proceedButtonEnabled.postValue(complete)
    }

}