package com.hypertrack.android.ui.screens.sign_up

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amazonaws.AmazonServiceException
import com.hypertrack.android.interactors.ConfirmationRequired
import com.hypertrack.android.interactors.LoginInteractor
import com.hypertrack.android.interactors.SignUpError
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.common.stringFromResource
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch

class SignUpViewModel(private val loginInteractor: LoginInteractor) : BaseViewModel() {

    val errorText = MutableLiveData<String?>()
    val page = MutableLiveData<Int>(0)

    fun onSignUpClicked(login: String, password: String, userAttributes: Map<String, String>) {
        when {
            password.length < 8 -> {
                page.postValue(SignUpFragment.PAGE_USER)
                errorText.postValue(R.string.password_too_short.stringFromResource())
            }
            !isEmail(login) -> {
                page.postValue(SignUpFragment.PAGE_USER)
                errorText.postValue(R.string.invalid_email.stringFromResource())
            }
            else -> {
                viewModelScope.launch {
                    val res = loginInteractor.signUp(login, password, userAttributes)
                    when (res) {
                        ConfirmationRequired -> {
                            destination.postValue(
                                SignUpFragmentDirections.actionSignUpFragmentToConfirmFragment(
                                    login
                                )
                            )
                        }
                        is SignUpError -> {
                            when (res.exception) {
                                is AmazonServiceException -> {
                                    errorText.postValue(res.exception.errorMessage)
                                }
                                else -> {
                                    errorText.postValue(res.exception.message)
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    fun onSignInClicked() {
        destination.postValue(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment(null))
    }

    fun onBackPressed(): Boolean {
        if (page.value == SignUpFragment.PAGE_INFO) {
            page.postValue(SignUpFragment.PAGE_USER)
            return true
        } else {
            return false
        }
    }

    fun onNextClicked(email: String, password: String) {
        if (isEmail(email)) {
            page.postValue(SignUpFragment.PAGE_INFO)
        } else {
            page.postValue(SignUpFragment.PAGE_USER)
            errorText.postValue(R.string.invalid_email.stringFromResource())
        }
    }

    private fun isEmail(s: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(s).matches()
    }

}