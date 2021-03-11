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

    fun onSignUpClicked(login: String, password: String) {
        when {
            password.length < 8 -> {
                errorText.postValue(R.string.password_too_short.stringFromResource())
            }
            !Patterns.EMAIL_ADDRESS.matcher(login).matches() -> {
                errorText.postValue(R.string.invalid_email.stringFromResource())
            }
            else -> {
                viewModelScope.launch {
                    val res = loginInteractor.signUp(login, password)
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

}