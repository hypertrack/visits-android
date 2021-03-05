package com.hypertrack.android.ui.screens.sign_in

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import com.hypertrack.android.interactors.LoginInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.ui.common.stringFromResource
import com.hypertrack.android.utils.*
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch

class SignInViewModel(
    private val loginInteractor: LoginInteractor
) : ViewModel() {

    private var login = ""
    private var password = ""

    val errorText = MutableLiveData<String>()
    val showProgress = MutableLiveData(false)
    val destination = MutableLiveData<NavDirections>()
    val isLoginButtonClickable = MutableLiveData(false)

    fun onLoginTextChanged(email: CharSequence) {
        // Log.v(TAG, "onLoginTextChanged $email")
        login = email.toString()
        enableButtonIfInputNonEmpty()
    }

    fun onPasswordTextChanged(pwd: CharSequence) {
        // Log.v(TAG, "onPasswordTextChanged $pwd")
        password = pwd.toString()
        enableButtonIfInputNonEmpty()
    }

    fun onLoginClick() {
        errorText.postValue("")
        // Log.v(TAG, "onLoginClick")
        isLoginButtonClickable.postValue(false)
        showProgress.postValue(true)

        viewModelScope.launch {
            val res = loginInteractor.signIn(login, password)
            when (res) {
                is PublishableKey -> {
                    if (res.key.isNotBlank()) {
                        showProgress.postValue(false)
                        destination.postValue(SignInFragmentDirections.actionSignInFragmentToVisitManagementFragment())
                    } else {
                        //todo task
                        Log.w(TAG, "Can't login with $login account")
                        errorText.postValue(MyApplication.context.getString(R.string.valid_publishable_key_required))
                        enableButtonIfInputNonEmpty()
                    }
                }
                else -> {
                    enableButtonIfInputNonEmpty()
                    showProgress.postValue(false)
                    when (res) {
                        is NoSuchUser -> {
                            errorText.postValue(R.string.user_does_not_exist.stringFromResource())
                        }
                        is InvalidLoginOrPassword -> {
                            errorText.postValue(R.string.incorrect_username_or_pass.stringFromResource())
                        }
                        else -> {
                            errorText.postValue(MyApplication.context.getString(R.string.unknown_error))
                        }
                    }
                }
            }
        }
    }

    private fun enableButtonIfInputNonEmpty() {
        // Log.v(TAG, "enableButtonIfInputNonEmpty")
        if (login.isNotBlank() && password.isNotBlank()) {
            // Log.v(TAG, "enabling Button")
            isLoginButtonClickable.postValue(true)
        }
    }

    companion object {
        const val TAG = "AccountLoginVM"
    }
}