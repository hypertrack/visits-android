package com.hypertrack.android.ui.screens.sign_up

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.ConfirmationRequired
import com.hypertrack.android.interactors.LoginInteractor
import com.hypertrack.android.interactors.SignUpError
import com.hypertrack.android.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class SignUpViewModel(private val loginInteractor: LoginInteractor) : BaseViewModel() {

    val errorText = MutableLiveData<String?>()

    fun signUp(login: String, password: String) {
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
                    //todo task
                    errorText.postValue(res.exception.message)
                }
            }
        }
    }

}