package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.AccountLoginProvider
import com.hypertrack.android.utils.Destination
import kotlinx.coroutines.launch

class AccountLoginViewModel(
        private val loginProvider: AccountLoginProvider,
        private val accountRepository: AccountRepository
) : ViewModel() {

    private var login = ""
    private var password = ""

    val showLoginFailureToast: LiveData<Boolean>
        get() = _showToast

    val showProgress: LiveData<Boolean>
        get() = _showProgress

    val destination: LiveData<Destination>
        get() = _destination

    val isLoginButtonClickable: LiveData<Boolean>
        get() = _isLoginButtonClickable

    private val _showProgress = MutableLiveData(false)

    private val _destination = MutableLiveData(Destination.LOGIN)

    private val _isLoginButtonClickable = MutableLiveData(false)

    private val _showToast = MutableLiveData(false)

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
        // Log.v(TAG, "onLoginClick")
        _isLoginButtonClickable.postValue(false)
        _showProgress.value = true
        viewModelScope.launch {
            val pk = loginProvider.getPublishableKey(login, password)
            if (pk.isNotBlank() && accountRepository.onKeyReceived(pk, "true")) {
                _destination.postValue(Destination.DRIVER_ID_INPUT)
            } else {
                // show error toast
                Log.w(TAG, "Can't login with $login account")
                _showToast.value = true
            }
            _showProgress.postValue(false)
        }

    }

    private fun enableButtonIfInputNonEmpty() {
        // Log.v(TAG, "enableButtonIfInputNonEmpty")
        if (login.isNotBlank() && password.isNotBlank() && _isLoginButtonClickable.value == false) {
            // Log.v(TAG, "enabling Button")
            _isLoginButtonClickable.postValue(true)
            _showToast.postValue(false)
        }
    }

    companion object {
        const val TAG = "AccountLoginVM"
    }
}