package com.hypertrack.android.ui.screens.confirm_email

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.*
import kotlinx.coroutines.launch

//todo task handle errors
class ConfirmEmailViewModel(private val loginInteractor: LoginInteractor) : ViewModel() {

    val loadingState = MutableLiveData<Boolean>()
    val errorText = MutableLiveData<String>()
    val destination = MutableLiveData<NavDirections>()

    fun onVerifiedClick() {
        loadingState.postValue(true)
        viewModelScope.launch {
            val res = loginInteractor.signInAfterVerify()
            loadingState.postValue(false)
            when (res) {
                is PublishableKey -> {
                    destination.postValue(ConfirmFragmentDirections.actionConfirmFragmentToVisitManagementFragment())
                }
                else -> {
                    if (res is LoginError) {
                        destination.postValue(ConfirmFragmentDirections.actionConfirmFragmentToSignInFragment())
//                        errorText.postValue(res.exception.message)
                    } else {
                        //todo task
                        errorText.postValue(res::class.java.simpleName)
                    }
                }
            }
        }
    }

    fun onResendClick() {
        loadingState.postValue(true)
        viewModelScope.launch {
            loginInteractor.resendEmailConfirmation()
            loadingState.postValue(false)
        }
    }

}