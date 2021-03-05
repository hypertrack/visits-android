package com.hypertrack.android.interactors

import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.*

interface LoginInteractor {
    suspend fun signIn(login: String, password: String): LoginResult
    suspend fun signUp(login: String, password: String)
    suspend fun resendEmailConfirmation()
}

class LoginInteractorImpl(
    private val accountLoginProvider: AccountLoginProvider,
    private val accountRepository: AccountRepository
) : LoginInteractor {

    override suspend fun signIn(login: String, password: String): LoginResult {
        val res = accountLoginProvider.getPublishableKey(login, password)
        return when (res) {
            is PublishableKey -> {
                val pkValid = accountRepository.onKeyReceived(res.key, "true")
                if (pkValid) {
                    res
                } else {
                    //todo task
                    LoginError(Exception("Invalid Publishable Key"))
                }
            }
            else -> {
                res
            }
        }
    }

    override suspend fun signUp(login: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun resendEmailConfirmation() {
        TODO("Not yet implemented")
    }
}

